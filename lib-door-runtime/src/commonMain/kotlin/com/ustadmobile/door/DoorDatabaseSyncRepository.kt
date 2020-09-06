package com.ustadmobile.door

import com.ustadmobile.door.entities.TableSyncStatus
import com.ustadmobile.door.entities.UpdateNotification

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
    suspend fun sync(tablesToSync: List<Int>?) : List<SyncResult>

    /**
     *
     */
    suspend fun findPendingUpdateNotifications(deviceId: Int): List<UpdateNotification>

    /**
     * Find a list of tables that need to be sync'd (e.g. those that have changed more recently than
     * a sync has been completed)
     */
    fun findTablesToSync(): List<TableSyncStatus>

    /**
     * Mark the given table id as having been changed at the specified time. This will be used by
     * the ClientSyncManager to determine which tables need to be synced.
     */
    suspend fun updateTableSyncStatusLastChanged(tableId: Int, lastChanged: Long)

    /**
     * Mark the given table id as having been synced at the specified time. This will be used by
     * the ClientSyncManager to determine which tables need to be synced.
     */
    suspend fun updateTableSyncStatusLastSynced(tableId: Int, lastSynced: Long)


    /**
     * Do not call this on the main thread: it might run a query
     */
    val clientId: Int
}