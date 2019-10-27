package com.ustadmobile.door

import io.ktor.client.HttpClient
import java.io.File

actual inline fun <reified  T> SyncableDoorDatabase.asRepository(context: Any, endpoint: String,
                                                                 accessToken: String,
                                                                 httpClient: HttpClient,
                                                                 attachmentsDir: String?): T {
    val dbClassName = this::class.java.canonicalName.replace("_JdbcKt", "")
    val dbClass = Class.forName(dbClassName)
    val repoImplClass = Class.forName("${dbClassName}_Repo") as Class<T>
    val attachmentsDirToUse = if(attachmentsDir != null){
        attachmentsDir
    }else {
        File("attachments").absolutePath //TODO: look this up from JNDI
    }
    val repo = repoImplClass
            .getConstructor(dbClass, String::class.java,String::class.java, HttpClient::class.java,
                    String::class.java)
            .newInstance(this, endpoint, accessToken, httpClient, attachmentsDirToUse)
    return repo
}