package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this method is the one to be used by sync methods to find all changes, searching
 * by local and master change sequence number ranges. This is used by the UmSyncIncoming method to
 * find all changes to send back.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UmSyncFindAllChanges {
}
