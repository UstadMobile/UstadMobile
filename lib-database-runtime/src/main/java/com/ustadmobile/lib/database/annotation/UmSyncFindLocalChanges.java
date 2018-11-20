package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used by the UmSyncOutgoing method to find local changes using the local change sequence number
 * (only).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UmSyncFindLocalChanges {
}
