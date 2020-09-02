package com.ustadmobile.door

import io.ktor.client.HttpClient

actual inline fun <reified  T: SyncableDoorDatabase> T.asRepository(context: Any,
                                                                    endpoint: String,
                                                                    accessToken: String,
                                                                    httpClient: HttpClient,
                                                                    attachmentsDir: String?,
                                                                    updateNotificationManager: UpdateNotificationManager?): T {
    return this
}
