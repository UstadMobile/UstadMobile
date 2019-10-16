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


}