package com.ustadmobile.core.domain.passkey

import com.ustadmobile.lib.db.entities.Person
import io.ktor.http.Url

data class CreatePasskeyParams(
    val username: String,
    val personUid: String,
    val doorNodeId: String,
    val usStartTime: Long,
    val serverUrl: String,
    val person: Person
){
    val domainName: String
        get() {
         return   Url(serverUrl).host
        }
}
