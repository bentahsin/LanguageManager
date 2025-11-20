package com.bentahsin.languagemanager;

import com.bentahsin.languagemanager.proxy.MessageProxyHandler;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Map;

/**
 * Kütüphanenin ana yönetim sınıfı.
 * Yapılandırılmış ve oluşturulmuş mesaj arayüzlerine erişim sağlar.
 */
@SuppressWarnings("unused")
public final class LanguageManager {

    private final JavaPlugin plugin;
    private final LanguageManagerBuilder builder;
    private final Map<Class<?>, Object> messageProxies;
    private final BukkitAudiences adventure;

    LanguageManager(JavaPlugin plugin, LanguageManagerBuilder builder, Map<Class<?>, Object> messageProxies, BukkitAudiences adventure) {
        this.plugin = plugin;
        this.builder = builder;
        this.messageProxies = messageProxies;
        this.adventure = adventure;
    }

    /**
     * Belirtilen mesaj arayüzünün kullanıma hazır bir örneğini döndürür.
     * @param messageInterface Mesaj arayüzünün sınıfı.
     * @param <T> Mesaj arayüzünün tipi.
     * @return Kullanıma hazır mesaj nesnesi.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> messageInterface) {
        Object proxy = messageProxies.get(messageInterface);
        if (proxy == null) {
            throw new IllegalArgumentException("Message interface " + messageInterface.getName() + " is not registered.");
        }
        return (T) proxy;
    }

    /**
     * Dosya tabanlı mesajları diskten yeniden yükler.
     * Değişikliklerin anında yansımasını sağlar.
     */
    public void reload() {
        plugin.getLogger().info("[LanguageManager] Reloading language files...");
        for (Map.Entry<Class<?>, Object> entry : messageProxies.entrySet()) {
            Object proxyHandler = java.lang.reflect.Proxy.getInvocationHandler(entry.getValue());
            if (proxyHandler instanceof MessageProxyHandler) {
                ((MessageProxyHandler) proxyHandler).reloadConfig(builder);
            }
        }
        plugin.getLogger().info("[LanguageManager] Reload complete.");
    }

    /**
     * Eklenti devre dışı bırakılırken çağrılmalıdır.
     * Adventure API kaynaklarını serbest bırakır.
     */
    public void disable() {
        if (this.adventure != null) {
            this.adventure.close();
        }
    }

    /**
     * LanguageManager oluşturmak için bir builder başlatır.
     * @param plugin Kütüphaneyi kullanan ana plugin.
     * @return Yeni bir LanguageManagerBuilder örneği.
     */
    public static LanguageManagerBuilder create(JavaPlugin plugin) {
        return new LanguageManagerBuilder(plugin);
    }
}