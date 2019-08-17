package com.ustadmobile.door

import io.ktor.client.HttpClient

actual inline fun <reified  T> SyncableDoorDatabase.asRepository(endpoint: String, accessToken: String, httpClient: HttpClient): T {
    val dbClassName = this::class.java.canonicalName!!.replace("_Impl", "")
    val dbClass = Class.forName(dbClassName)
    val repoImplClass = Class.forName("${dbClassName}_Repo") as Class<T>
    val repo = repoImplClass
            .getConstructor(dbClass, String::class.java,String::class.java, HttpClient::class.java)
            .newInstance(this, endpoint, accessToken, httpClient)
    return repo
}