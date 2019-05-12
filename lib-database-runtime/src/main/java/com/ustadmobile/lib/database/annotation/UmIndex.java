package com.ustadmobile.lib.database.annotation;

/**
 * Used to define a table level index with multiple fields
 */
public @interface UmIndex {

    String name() default "";

    boolean unique() default false;

    String[] value();

}
