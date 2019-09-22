package com.ustadmobile.door

import io.ktor.client.HttpClient

actual inline fun <reified  T> SyncableDoorDatabase.asRepository(endpoint: String, accessToken: String, httpClient: HttpClient): T {
    return this as T
}
