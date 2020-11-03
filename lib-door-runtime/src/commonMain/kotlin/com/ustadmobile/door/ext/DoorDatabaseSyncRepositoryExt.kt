//We don't directly use the receiver - but send updates should only be called on DoorDatabaseSyncRepository
@file:Suppress("unused")

package com.ustadmobile.door.ext

import com.github.aakira.napier.Napier
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.door.ServerUpdateNotificationManager
import com.ustadmobile.door.entities.UpdateNotification
import com.ustadmobile.door.entities.UpdateNotificationSummary
import com.ustadmobile.door.util.systemTimeInMillis

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