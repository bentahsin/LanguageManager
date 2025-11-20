package com.bentahsin.languagemanager.proxy;

import com.bentahsin.languagemanager.LanguageManagerBuilder;
import com.bentahsin.languagemanager.annotations.SystemMessages;
import com.bentahsin.languagemanager.util.PlaceholderUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gömülü (hard-coded) sistem mesajı arayüzlerinin metot çağrılarını yakalayan ve işleyen proxy sınıfı.
 * Kütüphanenin sistem mesajları modülünün beyni olarak çalışır.
 */
public class SystemMessageProxyHandler implements InvocationHandler {

    private final LanguageManagerBuilder builder;
    private final Map<String, Map<String, String>> translations;
    private final String activeLocale;
    private final String defaultLocale;

    /**
     * SystemMessageProxyHandler için kurucu metot.
     * @param builder Ana LanguageManagerBuilder.
     * @param config Arayüzden alınan @SystemMessages anotasyonu.
     * @param translations Ayrıştırılmış çeviriler.
     */
    public SystemMessageProxyHandler(LanguageManagerBuilder builder, SystemMessages config, Map<String, Map<String, String>> translations) {
        this.builder = builder;
        this.translations = translations;
        this.activeLocale = builder.getLocale();
        this.defaultLocale = config.defaultLocale();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Object.class)) {
            return method.invoke(this, args);
        }

        Map<String, String> methodTranslations = translations.get(method.getName());
        if (methodTranslations == null || methodTranslations.isEmpty()) {
            return "Missing @Translation definitions for method: " + method.getName();
        }

        String message = methodTranslations.get(activeLocale);
        if (message == null) {
            message = methodTranslations.get(defaultLocale);
        }

        if (message == null) {
            message = methodTranslations.values().iterator().next();
        }

        Class<?> returnType = method.getReturnType();
        String finalMessage = PlaceholderUtil.applyInternalPlaceholders(message, method, args);

        if (returnType.equals(Component.class)) {
            return builder.getMiniMessage().deserialize(finalMessage);
        }

        if (returnType.equals(List.class)) {
            return Arrays.stream(finalMessage.split("\n"))
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
        }

        return ChatColor.translateAlternateColorCodes('&', finalMessage);
    }
}