package com.ustadmobile.core.domain.passkey

import com.ustadmobile.lib.db.entities.Person
import io.ktor.http.Url
import com.ustadmobile.core.util.ext.formattedHost

data class CreatePasskeyParams(
    val username: String,
    val personUid: String,
    val doorNodeId: String,
    val usStartTime: Long,
    val serverUrl: String,
    val masterUrl: String,
    val person: Person
){
    val serverDomainName: String
        get() {
         return   Url(serverUrl).formattedHost()
        }

   val masterDomainName:String
       get() {
           return Url(masterUrl).formattedHost()
       }
}
