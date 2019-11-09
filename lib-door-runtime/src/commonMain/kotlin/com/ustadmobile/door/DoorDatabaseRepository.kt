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

    var connectivityStatus: Int

    companion object {

        const val STATUS_CONNECTED = 1

        const val STATUS_DISCONNECTED = 2
    }
}