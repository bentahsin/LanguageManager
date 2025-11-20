package com.bentahsin.languagemanager.proxy;

import com.bentahsin.languagemanager.LanguageManagerBuilder;
import com.bentahsin.languagemanager.annotations.Message;
import com.bentahsin.languagemanager.annotations.Messages;
import com.bentahsin.languagemanager.util.PlaceholderUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dosya tabanlı mesaj arayüzlerinin metot çağrılarını yakalayan ve işleyen proxy sınıfı.
 * Kütüphanenin dosya tabanlı modülünün beyni olarak çalışır.
 */
public class MessageProxyHandler implements InvocationHandler {

    private final LanguageManagerBuilder builder;
    private final Class<?> interfaceClass;
    private YamlConfiguration messages;
    private String prefix;

    public MessageProxyHandler(LanguageManagerBuilder builder, Class<?> interfaceClass) {
        this.builder = builder;
        this.interfaceClass = interfaceClass;
        this.reloadConfig(builder);
    }

    /**
     * Dil yapılandırmasını LanguageManagerBuilder'dan yeniden yükler.
     * Bu metot, canlı yeniden yükleme (hot-reload) işlevselliği için kullanılır.
     * @param builder Güncel LanguageManagerBuilder örneği.
     */
    public void reloadConfig(LanguageManagerBuilder builder) {
        Messages config = interfaceClass.getAnnotation(Messages.class);
        this.messages = builder.loadOrUpdateMessagesFile(interfaceClass, config);
        String rawPrefix = builder.getPrefix();
        this.prefix = (rawPrefix != null && !rawPrefix.isEmpty()) ? ChatColor.translateAlternateColorCodes('&', rawPrefix) : "";
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Object.class)) {
            return method.invoke(this, args);
        }

        Message messageInfo = method.getAnnotation(Message.class);
        if (messageInfo == null) {
            return "Invalid message method: " + method.getName();
        }

        Class<?> returnType = method.getReturnType();
        String messageKey = messageInfo.key();
        String defaultValue = messageInfo.value();

        OfflinePlayer papiPlayer = null;
        if (builder.isPapiHookEnabled() && args != null) {
            for (Object arg : args) {
                if (arg instanceof OfflinePlayer) {
                    papiPlayer = (OfflinePlayer) arg;
                    break;
                }
            }
        }

        if (returnType.equals(Component.class)) {
            String message = messages.getString(messageKey, defaultValue);
            String finalMessage = applyPlaceholders(message, method, args, papiPlayer);
            return builder.getMiniMessage().deserialize(finalMessage);
        }

        if (returnType.equals(List.class)) {
            List<String> messageList = messages.getStringList(messageKey);
            if (messageList.isEmpty()) {
                messageList = Collections.singletonList(defaultValue);
            }
            OfflinePlayer finalPapiPlayer = papiPlayer;
            return messageList.stream()
                    .map(line -> applyPlaceholdersAndColor(line, method, args, finalPapiPlayer))
                    .collect(Collectors.toList());
        }

        String message = messages.getString(messageKey, defaultValue);
        String finalMessage = applyPlaceholdersAndColor(message, method, args, papiPlayer);

        if (messageInfo.usePrefix() && !this.prefix.isEmpty()) {
            return this.prefix + finalMessage;
        }
        return finalMessage;
    }

    /**
     * Bir metin satırına hem iç placeholder'ları hem de PlaceholderAPI placeholder'larını uygular.
     */
    private String applyPlaceholders(String message, Method method, Object[] args, OfflinePlayer papiPlayer) {
        String result = PlaceholderUtil.applyInternalPlaceholders(message, method, args);
        if (builder.isPapiHookEnabled() && papiPlayer != null) {
            result = PlaceholderAPI.setPlaceholders(papiPlayer, result);
        }
        return result;
    }

    /**
     * applyPlaceholders metodunu çağırır ve ardından Bukkit renk kodlarını uygular.
     * @param message İşlenecek ham metin.
     * @param method Çağrılan arayüz metodu.
     * @param args Metoda geçirilen argümanlar.
     * @param papiPlayer PlaceholderAPI için kullanılacak oyuncu (null olabilir).
     * @return İşlenmiş ve renklendirilmiş son metin.
     */
    private String applyPlaceholdersAndColor(String message, Method method, Object[] args, OfflinePlayer papiPlayer) {
        String processedMessage = applyPlaceholders(message, method, args, papiPlayer);
        return ChatColor.translateAlternateColorCodes('&', processedMessage);
    }
}