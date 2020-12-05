//We don't directly use the receiver - but send updates should only be called on DoorDatabaseSyncRepository
@file:Suppress("unused")

package com.ustadmobile.door.ext

import com.github.aakira.napier.Napier
import com.ustadmobile.door.*
import com.ustadmobile.door.entities.UpdateNotification
import com.ustadmobile.door.entities.UpdateNotificationSummary
import com.ustadmobile.door.util.systemTimeInMillis
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * This is used by the generated SyncDao Repository when it is implementing
 * _findDevicesToNotify(EntityName) - the function which is called by dispatchUpdateNotifications
 * in order to generate UpdateNotification entities.
 */
fun DoorDatabaseSyncRepository.sendUpdates(tableId: Int, updateNotificationManager: ServerUpdateNotificationManager?,
                                           findDevicesFn: () -> List<UpdateNotificationSummary>,
                                            replaceUpdateNotificationFn: (List<UpdateNotification>) -> Unit)
        : List<UpdateNotificationSummary> {

    val devicesToNotify = findDevicesFn()
    if(devicesToNotify.isEmpty()) {
        Napier.d("[SyncRepo@${this.doorIdentityHashCode}]: sendUpdates: Table #$tableId has no devices to notify")
        return listOf()
    }

    Napier.v("[SyncRepo@${this.doorIdentityHashCode}]: sendUpdates: Table #$tableId needs to notify ${devicesToNotify.joinToString()}.",
        tag= DoorTag.LOG_TAG)

    val timeNow = systemTimeInMillis()
    val updateNotifications = devicesToNotify.map {
        UpdateNotification(pnDeviceId = it.deviceId, pnTableId = it.tableId, pnTimestamp = timeNow)
    }

    replaceUpdateNotificationFn(updateNotifications)
    updateNotificationManager?.onNewUpdateNotifications(updateNotifications)
    Napier.v("[SyncRepo@${this.doorIdentityHashCode}] replaced update notifications " +
            "and informed updatenotificationmanager: $updateNotificationManager", tag = DoorTag.LOG_TAG)


    return devicesToNotify
}

/**
 * Runs a block that syncs the given entity if it is in the list of tablesToSync
 * or tablesToSync is null (which means sync all entities)
 */
suspend inline fun DoorDatabaseSyncRepository.runEntitySyncIfRequired(tablesToSync: List<Int>?, tableId : Int,
                                           allResults: MutableList<SyncResult>,
                                           crossinline block: suspend DoorDatabaseSyncRepository.() -> SyncResult) {
    if(tablesToSync == null || tableId in tablesToSync) {
        allResults += block()
    }
}

/**
 * Records the
 */
suspend fun DoorDatabaseSyncRepository.recordSyncRunResult(allResults: List<SyncResult>) {
    val syncStatus = if(allResults.all { it.status == SyncResult.STATUS_SUCCESS}) {
        SyncResult.STATUS_SUCCESS
    }else {
        SyncResult.STATUS_FAILED
    }

    syncHelperEntitiesDao.insertSyncResult(SyncResult(status = syncStatus,
            timestamp = systemTimeInMillis()))
}

suspend inline fun <reified T:Any> DoorDatabaseSyncRepository.syncEntity(
    receiveRemoteEntitiesFn: suspend () -> List<T>,
    storeEntitiesFn: suspend (List<T>) -> Unit,
    findLocalUnsentEntitiesFn: suspend () -> List<T>,
    entityToAckFn: (entities: List<T>, primary: Boolean) -> List<EntityAck>): SyncResult {

    val newEntities = receiveRemoteEntitiesFn()
    storeEntitiesFn(newEntities)

    val entityAcks = entityToAckFn(newEntities, true)
    httpClient.postEntityAck(entityAcks, endpoint, dbPath, db)

    val dbName = this::class.simpleName
    val entityName =  T::class.simpleName

    val localUnsentEntities = findLocalUnsentEntitiesFn()

    httpClient.post<Unit> {
        url {
            takeFrom(endpoint)
            encodedPath = "$encodedPath$dbPath/${dbName}_SyncDao/_replace${entityName}"
        }

        dbVersionHeader(db)
        body = defaultSerializer().write(localUnsentEntities,
                ContentType.Application.Json.withUtf8Charset())
    }

    val result = SyncResult(received = newEntities.size, sent = localUnsentEntities.size)

    syncHelperEntitiesDao.insertSyncResult(result)

    return result
}