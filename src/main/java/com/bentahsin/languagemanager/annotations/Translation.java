package com.bentahsin.languagemanager.annotations;

import java.lang.annotation.*;

/**
 * Bir sistem mesajı metodu için belirli bir dildeki çeviriyi tanımlar.
 * Bu anotasyon, bir metot üzerinde birden çok kez kullanılabilir.
 */
@Repeatable(Translations.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Translation {
    /**
     * Bu çevirinin ait olduğu dil kodu (örn: "tr", "en").
     */
    String locale();

    /**
     * Bu dildeki mesaj metni. Çoklu satırlar için \n kullanılabilir.
     */
    String value();
}