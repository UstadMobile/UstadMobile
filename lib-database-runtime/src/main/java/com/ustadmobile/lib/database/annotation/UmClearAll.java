package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be applied to an abstract method on a class with the @UmDatabase annotation.
 * This generated method will clear all data from all tables. It will not delete the tables themselves.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UmClearAll {
}
