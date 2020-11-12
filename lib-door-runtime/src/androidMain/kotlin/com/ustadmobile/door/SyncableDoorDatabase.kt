package com.ustadmobile.door

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.room.RoomDatabase
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
    val dbName = (this as RoomDatabase).openHelper.databaseName
    val attachmentsDirToUse = if(attachmentsDir == null) {
        File(ContextCompat.getExternalFilesDirs(context as Context, null)[0],
                "$dbName/attachments").absolutePath
    }else {
        attachmentsDir
    }

    val dbClassName = this::class.java.canonicalName!!.replace("_Impl", "")
    val dbClass = Class.forName(dbClassName)
    val repoImplClass = Class.forName("${dbClassName}_Repo") as Class<T>
    val repo = repoImplClass
            .getConstructor(dbClass, String::class.java,String::class.java, HttpClient::class.java,
                    String::class.java, ServerUpdateNotificationManager::class.java, Boolean::class.javaPrimitiveType)
            .newInstance(this, endpoint, accessToken, httpClient, attachmentsDirToUse,
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
    val wrapperClass = Class.forName("${dbClass.qualifiedName}_DbSyncableReadOnlyWrapper") as Class<T>
    return wrapperClass.getConstructor(dbClass.java).newInstance(this)
}

@Suppress("UNCHECKED_CAST")
actual fun <T: SyncableDoorDatabase> T.unwrap(dbClass: KClass<T>): T {
    if(this is DoorDatabaseSyncableReadOnlyWrapper) {
        return this.realDatabase as T
    }else {
        return this
    }
}
