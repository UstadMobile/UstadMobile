package com.ustadmobile.lib.database.annotation;

/**
 * Created by mike on 1/21/18.
 */

public @interface UmEntity {

    String[] primaryKeys() default {};

    UmIndex[] indices() default {};

}
