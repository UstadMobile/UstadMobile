package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Used to indicate that the given method should be accessible over HTTP REST
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface UmRestAccessible {

    int timeout() default 5000;
}
