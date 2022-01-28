package com.ustadmobile.core.assignment

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.IncomingReplicationEvent
import com.ustadmobile.door.IncomingReplicationListener
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ClazzAssignment
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

/**
 * SyncListener that will handle when an incoming ClazzAssignment change requires a recheck of
 * ClazzAssignmentRollUp generation. E.g. When a assignment is created on a client device.
 */
class ClazzAssignmentIncomingReplicationListener(
    val site: Endpoint,
    val di: DI
): IncomingReplicationListener {

    val db: UmAppDatabase by di.on(site).instance(tag = DoorTag.TAG_DB)

    override suspend fun onIncomingReplicationProcessed(incomingReplicationEvent: IncomingReplicationEvent) {
        if(incomingReplicationEvent.tableId == ClazzAssignment.TABLE_ID) {
            val clazzAssignmentUids = incomingReplicationEvent.incomingReplicationData
                .mapNotNull { it.jsonObject["caUid"]?.jsonPrimitive?.longOrNull }
            clazzAssignmentUids.chunked(100).forEach {
                db.clazzAssignmentRollUpDao.invalidateCacheByAssignmentList(it)
            }
        }
    }
}