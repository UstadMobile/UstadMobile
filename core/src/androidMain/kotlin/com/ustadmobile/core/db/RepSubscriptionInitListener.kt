package com.ustadmobile.core.db

import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.withDoorTransaction
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.replication.ReplicationSubscriptionManager

/**
 * When the client connects to a remote node (e.g. the primary server or an intermediary device) then
 * we need to setup grants etc so that data will be replicated to it.
 *
 * After this runs, the ReplicationSubscriptionListener is going to call onNewDoorNode that should
 * find anything that needs to be replicated.
 *
 */
class RepSubscriptionInitListener : ReplicationSubscriptionManager.SubscriptionInitializedListener {

    override suspend fun onSubscriptionInitialized(
        repo: DoorDatabaseRepository,
        remoteNodeId: Long
    ) {
        repo.db.withDoorTransactionAsync(UmAppDatabase::class) { transactDb ->
            //check for the session

        }
    }
}