package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method should be generated to send a sync to the corresponding
 * UmSyncIncoming method for this DAO. Parameters as follows:
 * D - it's own DAO type
 * accountPersonUid - personUid for the the account being used to run the sync
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UmSyncOutgoing {
}
