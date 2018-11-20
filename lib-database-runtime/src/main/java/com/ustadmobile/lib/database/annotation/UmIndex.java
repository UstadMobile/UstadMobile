package com.ustadmobile.lib.database.annotation;

/**
 * Created by mike on 1/24/18.
 */

public @interface UmIndex {

    String name() default "";

    boolean unique() default false;

    String[] value();

}
