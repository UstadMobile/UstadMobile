package com.ustadmobile.door

import com.ustadmobile.door.entities.TableSyncStatus
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
    suspend fun sync(tablesToSync: List<Int>?) : List<SyncResult>

    /**
     * This will be implemented by generated code to run the query. It will find a list of all
     * pending UpdateNotification entities for the given deviceId (e.g. used to find the backlog
     * of notifications when a client subscribes to events).
     */
    suspend fun findPendingUpdateNotifications(deviceId: Int): List<UpdateNotification>

    /**
     * This will delete the matching UpdateNotification. It should be called after an update notification
     * has been delivered to the client (via the client making an http request answered by
     * lib-door-runtime RespondUpdateNotifications.respondUpdateNotificationReceived).
     *
     * Note this is not done using the actual notification uid because this is not known when
     * the server sends it live
     *
     * @param deviceId The deviceid as per the UpdateNotification
     * @param tableId The tableId as per the UpdateNotification
     * @param lastModTimestamp The pnTimestamp as per the UpdateNotification
     */
    suspend fun deleteUpdateNotification(deviceId: Int, tableId: Int, lastModTimestamp: Long)

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
     * Listen for incoming sync changes. This can be used to trigger logic that is required to
     * update clients (e.g. when a permission change happens granting a client access to an entity
     * it didn't have access to before).
     */
    fun <T : Any> addSyncListener(entityClass: KClass<T>, listener: SyncListener<T>)

    /**
     * This is to be called from generated code on the SyncDao's HTTP Endpoint (e.g.
     * DbNameSyncDao_KtorRoute). It is called after entities are received from an incoming sync. It
     * will trigger any SyncListeners that were added using addSyncListener
     *
     * @param entityClass
     */
    fun <T: Any> handleSyncEntitiesReceived(entityClass: KClass<T>, entities: List<T>)

    /**
     * Do not call this on the main thread: it might run a query
     */
    val clientId: Int
}