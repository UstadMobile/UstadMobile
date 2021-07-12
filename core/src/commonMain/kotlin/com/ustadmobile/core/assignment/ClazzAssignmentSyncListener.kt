package com.ustadmobile.core.assignment

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.SyncEntitiesReceivedEvent
import com.ustadmobile.door.SyncListener
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ClazzAssignment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

/**
 * SyncListener that will handle when an incoming ClazzAssignment change requires a recheck of
 * ClazzAssignmentRollUp generation. E.g. When a assignment is created on a client device.
 */
class ClazzAssignmentSyncListener(val site: Endpoint, val di: DI) {

    val db: UmAppDatabase by di.on(site).instance(tag = DoorTag.TAG_DB)

    val assignmentListener = object: SyncListener<ClazzAssignment> {
        override fun onEntitiesReceived(evt: SyncEntitiesReceivedEvent<ClazzAssignment>) {
            GlobalScope.launch {
                val clazzAssignmentUids = evt.entitiesReceived.map { it.caUid }.distinct()
                clazzAssignmentUids.chunked(100).forEach {
                    db.clazzAssignmentRollUpDao.invalidateCacheByAssignmentList(it)
                }
            }
        }
    }

}