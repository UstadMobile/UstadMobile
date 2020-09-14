package com.ustadmobile.door

import io.ktor.client.HttpClient

interface SyncableDoorDatabase {

    val master: Boolean

}

/**
 * Get a repository for the given database. This should be kept as a singleton.
 *
 * @param context system context
 * @param endpoint The endpoint url for the primary server e.g. http://server.name:1234/ (always use a trailing slash)
 * @param accessToken unused
 * @param httpClient Ktor HttpClient for http requests
 * @param updateNotificationManager ServerUpdateNotificationManager to use if this is a server instance
 * responsible to distribute update notifications
 * @param useClientSyncManager if true, the underlying repository will automatically create a
 * ClientSyncManager and connect to server sent events to receive immediate updates.
 */
expect inline fun <reified  T: SyncableDoorDatabase> T.asRepository(context: Any,
                                                                 endpoint: String,
                                                                 accessToken: String,
                                                                 httpClient: HttpClient,
                                                                 attachmentsDir: String? = null,
                                                                 updateNotificationManager: ServerUpdateNotificationManager? = null,
                                                                 useClientSyncManager: Boolean  = false): T
