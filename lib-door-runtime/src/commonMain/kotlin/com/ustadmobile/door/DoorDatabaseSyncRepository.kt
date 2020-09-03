package com.ustadmobile.door

import com.ustadmobile.door.entities.UpdateNotification
import kotlin.reflect.KClass

interface DoorDatabaseSyncRepository: DoorDatabaseRepository {

    suspend fun sync(tablesToSync: List<KClass<*>>?)

    suspend fun findPendingUpdateNotifications(deviceId: Int): List<UpdateNotification>

    /**
     * Do not call this on the main thread: it might run a query
     */
    val clientId: Int
}