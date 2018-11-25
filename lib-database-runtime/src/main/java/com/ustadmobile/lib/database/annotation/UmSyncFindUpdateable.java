package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used by the UmSyncIncoming annotated method to determine which entities, from a given list of
 * entities or primary keys, exist, and out of those, which of them can be updated by a given user
 * account.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UmSyncFindUpdateable {
}
