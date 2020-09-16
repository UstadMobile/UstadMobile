package com.ustadmobile.door

import io.ktor.client.HttpClient
import java.io.File

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
    val repo = repoImplClass
            .getConstructor(dbClass.java, String::class.java,String::class.java, HttpClient::class.java,
                    String::class.java, ServerUpdateNotificationManager::class.java, Boolean::class.javaPrimitiveType)
            .newInstance(this, endpoint, accessToken, httpClient, attachmentsDirToUse,
                    updateNotificationManager, useClientSyncManager)
    return repo
}