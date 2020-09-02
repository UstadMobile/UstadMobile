package com.ustadmobile.door

import io.ktor.client.HttpClient

interface SyncableDoorDatabase {

    val master: Boolean

}

expect inline fun <reified  T: SyncableDoorDatabase> T.asRepository(context: Any,
                                                                 endpoint: String,
                                                                 accessToken: String,
                                                                 httpClient: HttpClient,
                                                                 attachmentsDir: String? = null,
                                                                 updateNotificationManager: UpdateNotificationManager? = null): T
