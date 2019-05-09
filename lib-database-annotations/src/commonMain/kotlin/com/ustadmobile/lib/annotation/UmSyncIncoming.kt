package com.ustadmobile.lib.database.annotation

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
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.BINARY)
annotation class UmSyncIncoming {
    companion object {

        const val ACTION_UPDATE = 1

        const val ACTION_INSERT = 2

        const val ACTION_REJECT = 3
    }

}
