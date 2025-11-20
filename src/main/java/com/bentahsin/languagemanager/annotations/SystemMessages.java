package com.bentahsin.languagemanager.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bir arayüzü, tamamen kod içinde tanımlanmış, dosyaya bağımlı olmayan
 * bir sistem mesajı paketi olarak işaretler.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SystemMessages {
    /**
     * Varsayılan dil kodu (locale).
     * Belirtilen dilde çeviri bulunamazsa bu dil kullanılır.
     */
    String defaultLocale() default "en";
}