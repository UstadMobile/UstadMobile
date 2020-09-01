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
     * database. This function should be called by the underlying db system (e.g. SQLite or Postgres)
     * when there are
     */
    fun onPendingChangeLog(tableIds: Set<Int>)

    /**
     * This function will be generated on all repositories. It will dispatch Push Notifications for
     * values that are in the changelog.
     */
    suspend fun dispatchPushNotifications(tableId: Int)



    companion object {

        const val STATUS_CONNECTED = 1

        const val STATUS_DISCONNECTED = 2
    }
}