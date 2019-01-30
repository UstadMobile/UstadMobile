package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this method should be the generated handleIncomingSync method. The generated
 * method will check entities submitted for a sync, and return changes. It should return a SyncResult
 * and have the following parameters:
 *
 * List&lt;T&gt; incomingChanges - items that were locally changed on the other side
 * long fromLocalChangeSeqNum - currently unused. If this is a local-local (non-master) sync
 * long fromMasterChangeSeqNum - return all entities that have been changed since masterChangeSeqNum
 * long accountPersonUid - uid of the account that is running the sync (for permission checking)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UmSyncIncoming {

    int ACTION_UPDATE = 1;

    int ACTION_INSERT = 2;

    int ACTION_REJECT = 3;

}
