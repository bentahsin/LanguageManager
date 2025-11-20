package com.bentahsin.languagemanager.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bir arayüzü, dosya tabanlı bir mesaj paketi olarak işaretler.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Messages {
    /**
     * Oluşturulacak/okunacak dil dosyalarının öneki.
     * Örn: "messages" ise dosyalar "messages_tr.yml", "messages_en.yml" olur.
     */
    String filePrefix() default "messages";

    /**
     * Dosya bulunamazsa veya eksikse kullanılacak varsayılan dil kodu (locale).
     */
    String defaultLocale() default "en";
}