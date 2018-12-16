package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the given parameter represents the person uid for the current, authorized user.
 * If this query is being sent over the network, we must validate their authorization.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface UmRestAuthorizedUidParam {

    String headerName() default "X-Auth-Token";

}
