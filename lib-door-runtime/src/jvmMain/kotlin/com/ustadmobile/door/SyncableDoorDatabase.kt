package com.ustadmobile.door

import io.ktor.client.HttpClient
import java.io.File
import kotlin.reflect.KClass

actual inline fun <reified  T: SyncableDoorDatabase> T.asRepository(context: Any,
                                                                    endpoint: String,
                                                                    accessToken: String,
                                                                    httpClient: HttpClient,
                                                                    attachmentsDir: String?,
                                                                    updateNotificationManager: ServerUpdateNotificationManager?,
                                                                    useClientSyncManager: Boolean): T {
    val dbClass = T::class
    val repoImplClass = Class.forName("${dbClass.qualifiedName}_Repo") as Class<T>
    val attachmentsDirToUse = if(attachmentsDir != null){
        attachmentsDir
    }else {
        File("attachments").absolutePath //TODO: look this up from JNDI
    }

    val db = if(this is SyncableDoorDatabaseWrapper<*>) {
        this.unwrap(dbClass)
    }else {
        this
    }

    val repo = repoImplClass
            .getConstructor(dbClass.java, String::class.java,String::class.java, HttpClient::class.java,
                    String::class.java, ServerUpdateNotificationManager::class.java, Boolean::class.javaPrimitiveType)
            .newInstance(db, endpoint, accessToken, httpClient, attachmentsDirToUse,
                    updateNotificationManager, useClientSyncManager)
    return repo
}

/**
 * Wrap a syncable database to prevent accidental use of the database instead of the repo on queries
 * that modify syncable entities. All modification queries (e.g. update, insert etc) must be done on
 * the repo.
 */
@Suppress("UNCHECKED_CAST")
actual fun <T: SyncableDoorDatabase> T.wrap(dbClass: KClass<T>) : T {
    val wrapperClass = Class.forName("${dbClass.qualifiedName}_DbWrapper") as Class<T>
    return wrapperClass.getConstructor(dbClass.java).newInstance(this)
}

@Suppress("UNCHECKED_CAST")
actual fun <T: SyncableDoorDatabase> T.unwrap(dbClass: KClass<T>): T {
    return (this as SyncableDoorDatabaseWrapper<*>).realDatabase as T
}
