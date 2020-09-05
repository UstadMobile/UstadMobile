package com.ustadmobile.door

import com.ustadmobile.door.entities.UpdateNotification
import kotlin.reflect.KClass

/**
 * Interface implemented by all repositories that have syncable entities.
 *
 *
 *
 */
interface DoorDatabaseSyncRepository: DoorDatabaseRepository {

    /**
     * Run a sync of the tables in the given list. If null, sync all tables
     */
    suspend fun sync(tablesToSync: List<Int>?)

    suspend fun findPendingUpdateNotifications(deviceId: Int): List<UpdateNotification>

    /**
     * Do not call this on the main thread: it might run a query
     */
    val clientId: Int
}