package com.ustadmobile.door

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.room.RoomDatabase
import io.ktor.client.HttpClient
import java.io.File

actual inline fun <reified  T> SyncableDoorDatabase.asRepository(context: Any, endpoint: String,
                                                                 accessToken: String,
                                                                 httpClient: HttpClient,
                                                                 attachmentsDir: String?): T {
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
                    String::class.java)
            .newInstance(this, endpoint, accessToken, httpClient, attachmentsDirToUse)
    return repo
}