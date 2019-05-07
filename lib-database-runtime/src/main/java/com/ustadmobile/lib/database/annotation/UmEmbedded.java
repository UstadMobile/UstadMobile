package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by mike on 1/25/18.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface UmEmbedded {
}
