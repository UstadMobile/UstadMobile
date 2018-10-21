package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by mike on 1/21/18.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface UmEntity {

    String[] primaryKeys() default {};

    UmIndex[] indices() default {};

    int tableId() default 0;

}
