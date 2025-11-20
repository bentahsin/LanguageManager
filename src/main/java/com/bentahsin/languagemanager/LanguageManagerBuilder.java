package com.bentahsin.languagemanager;

import com.bentahsin.languagemanager.annotations.*;
import com.bentahsin.languagemanager.proxy.MessageProxyHandler;
import com.bentahsin.languagemanager.proxy.SystemMessageProxyHandler;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * LanguageManager'ı yapılandırmak ve oluşturmak için kullanılan akıcı API.
 * Kütüphanenin başlatılması için ana giriş noktasıdır.
 */
@SuppressWarnings("unused")
public final class LanguageManagerBuilder {

    private final JavaPlugin plugin;
    private final Set<Class<?>> messageInterfaces = new HashSet<>();
    private String locale = "en";
    private String prefix = "";
    private boolean papiHook = false;
    private BukkitAudiences adventure;
    private MiniMessage miniMessage;

    public LanguageManagerBuilder(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Oyuncular için kullanılacak dil kodunu (locale) ayarlar.
     * Örn: "tr", "en", "de".
     * @param locale Dil kodu.
     * @return Zincirleme için builder'ın kendisi.
     */
    public LanguageManagerBuilder withLocale(String locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Dosya tabanlı mesajların başına eklenecek bir önek (prefix) ayarlar.
     * @param prefix Mesaj öneki (renk kodları desteklenir).
     * @return Zincirleme için builder'ın kendisi.
     */
    public LanguageManagerBuilder withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Kütüphaneye yönetilecek mesaj arayüzlerini kaydeder.
     * @param interfaces Kaydedilecek arayüz sınıfları.
     * @return Zincirleme için builder'ın kendisi.
     */
    public LanguageManagerBuilder register(Class<?>... interfaces) {
        this.messageInterfaces.addAll(Arrays.asList(interfaces));
        return this;
    }

    /**
     * PlaceholderAPI desteğini etkinleştirir.
     * Sunucuda PAPI yüklü değilse, bu ayar yoksayılır.
     * @return Zincirleme için builder'ın kendisi.
     */
    public LanguageManagerBuilder withPapiHook() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.papiHook = true;
        } else {
            plugin.getLogger().warning("[LanguageManager] PlaceholderAPI hook enabled, but PlaceholderAPI not found.");
        }
        return this;
    }

    /**
     * Yapılandırmayı tamamlar ve kullanıma hazır bir LanguageManager örneği oluşturur.
     * @return Yeni LanguageManager örneği.
     */
    public LanguageManager build() {
        this.adventure = BukkitAudiences.create(plugin);
        this.miniMessage = MiniMessage.miniMessage();

        Map<Class<?>, Object> builtProxies = new HashMap<>();

        for (Class<?> interfaceClass : messageInterfaces) {
            Object proxy;
            if (interfaceClass.isInterface() && interfaceClass.isAnnotationPresent(Messages.class)) {
                proxy = Proxy.newProxyInstance(
                        interfaceClass.getClassLoader(),
                        new Class[]{interfaceClass},
                        new MessageProxyHandler(this, interfaceClass)
                );

            } else if (interfaceClass.isInterface() && interfaceClass.isAnnotationPresent(SystemMessages.class)) {
                SystemMessages config = interfaceClass.getAnnotation(SystemMessages.class);
                Map<String, Map<String, String>> translations = parseSystemMessages(interfaceClass);
                proxy = Proxy.newProxyInstance(
                        interfaceClass.getClassLoader(),
                        new Class[]{interfaceClass},
                        new SystemMessageProxyHandler(this, config, translations)
                );

            } else {
                plugin.getLogger().warning("[LanguageManager] " + interfaceClass.getName() + " is not a valid message interface. Skipping.");
                continue;
            }
            builtProxies.put(interfaceClass, proxy);
        }
        return new LanguageManager(plugin, this, builtProxies, adventure);
    }

    /**
     * Belirtilen arayüz için dil dosyasını yükler veya oluşturur/günceller.
     * @param interfaceClass İşlenecek mesaj arayüzü.
     * @param config Arayüzün @Messages anotasyonu.
     * @return Yüklenmiş ve güncellenmiş YamlConfiguration nesnesi.
     */
    public YamlConfiguration loadOrUpdateMessagesFile(Class<?> interfaceClass, Messages config) {
        String fileName = config.filePrefix() + "_" + locale + ".yml";
        File messageFile = new File(plugin.getDataFolder(), fileName);
        YamlConfiguration yamlConfig = new YamlConfiguration();
        boolean modified = false;

        try {
            if (!messageFile.exists()) {
                plugin.getLogger().info("[LanguageManager] Creating default language file: " + fileName);
                for (Method method : interfaceClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Message.class)) {
                        Message message = method.getAnnotation(Message.class);
                        yamlConfig.set(message.key(), message.value());
                    }
                }
                yamlConfig.save(messageFile);
            } else {
                yamlConfig.load(messageFile);
                for (Method method : interfaceClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Message.class)) {
                        Message message = method.getAnnotation(Message.class);
                        if (!yamlConfig.contains(message.key())) {
                            yamlConfig.set(message.key(), message.value());
                            modified = true;
                        }
                    }
                }
                if (modified) {
                    plugin.getLogger().info("[LanguageManager] Updating language file with new messages: " + fileName);
                    yamlConfig.save(messageFile);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("[LanguageManager] An error occurred with language file: " + fileName);
            plugin.getLogger().severe(e.getMessage());
        }
        return yamlConfig;
    }

    /**
     * Bir sistem mesajı arayüzündeki @Translation anotasyonlarını ayrıştırır.
     * @param interfaceClass İşlenecek sistem mesajı arayüzü.
     * @return Metot adına göre gruplanmış çeviriler haritası.
     */
    private Map<String, Map<String, String>> parseSystemMessages(Class<?> interfaceClass) {
        Map<String, Map<String, String>> translations = new HashMap<>();
        for (Method method : interfaceClass.getDeclaredMethods()) {
            Map<String, String> methodTranslations = new HashMap<>();
            Translation[] annotations = method.getAnnotationsByType(Translation.class);
            for (Translation t : annotations) {
                methodTranslations.put(t.locale(), t.value());
            }
            if (!methodTranslations.isEmpty()) {
                translations.put(method.getName(), methodTranslations);
            }
        }
        return translations;
    }

    public JavaPlugin getPlugin() { return plugin; }
    public String getLocale() { return locale; }
    public String getPrefix() { return prefix; }
    public boolean isPapiHookEnabled() { return papiHook; }
    public MiniMessage getMiniMessage() { return miniMessage; }
}