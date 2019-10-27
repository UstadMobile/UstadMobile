package com.ustadmobile.door

import io.ktor.client.HttpClient

interface SyncableDoorDatabase {

    val master: Boolean

}

expect inline fun <reified  T> SyncableDoorDatabase.asRepository(context: Any,
                                                                 endpoint: String,
                                                                 accessToken: String,
                                                                 httpClient: HttpClient,
                                                                 attachmentsDir: String? = null): T
