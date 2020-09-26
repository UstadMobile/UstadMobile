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
     * This will be implemented by generated code to run the query. It will find a list of all
     * pending UpdateNotification entities for the given deviceId (e.g. used to find the backlog
     * of notifications when a client subscribes to events).
     */
    suspend fun findPendingUpdateNotifications(deviceId: Int): List<UpdateNotification>

    /**
     * This will be implemented by generated code to run the query. It will find a list of any
     * tableIds that have pending ChangeLog items that should be sent to dispatchUpdateNotifications.
     * This is used on startup to find any changes that happen when ChangeLogMonitor was not running.
     *
     * @return A list of tableIds for which there are pending ChangeLogs
     */
    suspend fun findTablesWithPendingChangeLogs(): List<Int>


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

    suspend fun selectNextSqliteSyncablePk(tableId: Int): Long

    suspend fun incrementNextSqliteSyncablePk(tableId: Int, increment: Int)

    suspend fun getAndIncrementSqlitePk(tableId: Int, increment: Int): Long


    /**
     * Do not call this on the main thread: it might run a query
     */
    val clientId: Int
}