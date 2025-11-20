package com.bentahsin.languagemanager.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bir arayüz metodunu, belirli bir YAML anahtarı ve varsayılan değer ile eşleştirir.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Message {
    /**
     * Dil dosyasındaki YAML yolu (key).
     */
    String key();

    /**
     * Bu anahtar dosyada bulunmuyorsa, dosyaya yazılacak ve kullanılacak varsayılan değer.
     * Renk kodları için '&' kullanılabilir.
     */
    String value();

    /**
     * Eğer true ise, bu mesaja LanguageManager'da tanımlanan genel prefix eklenir.
     * @return Prefix'in eklenip eklenmeyeceği.
     */
    boolean usePrefix() default true;
}