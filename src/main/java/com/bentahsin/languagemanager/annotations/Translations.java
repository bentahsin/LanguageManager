package com.bentahsin.languagemanager.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Translation anotasyonunun birden çok kez kullanılabilmesi için gerekli olan
 * container (taşıyıcı) anotasyon.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Translations {
    Translation[] value();
}