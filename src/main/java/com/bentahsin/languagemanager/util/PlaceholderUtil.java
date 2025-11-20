package com.bentahsin.languagemanager.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Placeholder işlemlerini merkezi olarak yöneten yardımcı sınıf.
 */
public final class PlaceholderUtil {

    /**
     * Bu sınıfın bir örneğinin oluşturulmasını engellemek için private constructor.
     */
    private PlaceholderUtil() {
    }

    /**
     * Bir metin satırına, bir metodun parametre isimlerini ve argümanlarını kullanarak
     * iç placeholder'ları uygular.
     * <p>
     * Örnek: `welcomeMessage(String oyuncuAdi)` metodu "Notch" argümanı ile çağrıldığında,
     * metin içindeki `{oyuncuAdi}` ifadesi "Notch" ile değiştirilir.
     *
     * @param message İşlenecek ham metin.
     * @param method Çağrılan arayüz metodu.
     * @param args Metoda geçirilen argümanlar.
     * @return Placeholder'ları işlenmiş metin.
     */
    public static String applyInternalPlaceholders(String message, Method method, Object[] args) {
        if (args == null || args.length == 0) {
            return message;
        }

        String result = message;
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            String placeholder = "{" + parameters[i].getName() + "}";
            result = result.replace(placeholder, String.valueOf(args[i]));
        }
        return result;
    }
}