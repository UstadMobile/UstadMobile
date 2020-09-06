package com.ustadmobile.door

import io.ktor.client.HttpClient

/**
 * Common interface that is implemented by any DAO repository. Can be used to get info including
 * the active endpoint, auth, database path and the http client.
 */
interface DoorDatabaseRepository {

    val endpoint: String

    val auth: String

    val dbPath: String

    val httpClient: HttpClient

    suspend fun addMirror(mirrorEndpoint: String, initialPriority: Int): Int

    suspend fun removeMirror(mirrorId: Int)

    suspend fun updateMirrorPriorities(newPriorities: Map<Int, Int>)

    suspend fun activeMirrors(): List<MirrorEndpoint>

    /**
     * Adds a weak reference to the given connectivity listener - useful for RepositoryLoadHelper
     * so it can automatically retry requests when connectivity is restored or when a mirror
     * becomes available.
     */
    fun addWeakConnectivityListener(listener: RepositoryConnectivityListener)

    /**
     *
     */
    fun removeWeakConnectivityListener(listener: RepositoryConnectivityListener)

    var connectivityStatus: Int

    /**
     * This map will be a generated map of table names (e.g. EntityName) to the corresponding TableId
     * for all syncable entities
     */
    val tableIdMap: Map<String, Int>

    /**
     * This function will be generated on all repositories. It is intended for use on the primary
     * database (e.g. the server). This function should be called by something which is listening
     * for database changes (e.g. RepositoryUpdateDispatcher). It is best for these to be dispatched
     * using a fan-out pattern.
     *
     * This function should be called periodically after a change happens (e.g. as is done by
     * the ChangeLogMonitor) and NOT once for every single row (for performance reasons).
     */
    fun onPendingChangeLog(tableIds: Set<Int>)

    /**
     * This function will be generated on all repositories. It will dispatch update notifications
     * for values that are in the changelog for that table. It will use the notifyOnUpdate query
     * that is on the SyncableEntity annotation of an entity to find which devices should be
     * notified of changes. This will result in creating / updating the UpdateNotification table.
     *
     * It will also call the UpdateNotificationManager (if provided) so that any client which is
     * currently subscribed for updates will be notified.
     */
    suspend fun dispatchUpdateNotifications(tableId: Int)

    fun addTableChangeListener(listener: TableChangeListener)

    fun removeTableChangeListener(listener: TableChangeListener)

    fun handleTableChanged(tableName: String)

    companion object {

        const val STATUS_CONNECTED = 1

        const val STATUS_DISCONNECTED = 2
    }
}