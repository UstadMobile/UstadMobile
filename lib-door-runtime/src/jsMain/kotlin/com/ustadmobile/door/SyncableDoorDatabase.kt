package com.ustadmobile.door

import io.ktor.client.HttpClient

actual inline fun <reified  T> SyncableDoorDatabase.asRepository(context: Any,
                                                                 endpoint: String,
                                                                 accessToken: String,
                                                                 httpClient: HttpClient,
                                                                 attachmentsDir: String?): T {
    return this as T
}
