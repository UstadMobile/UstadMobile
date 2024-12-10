package com.ustadmobile.core.domain.passkey

import com.ustadmobile.lib.db.entities.Person

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
            val domain = serverUrl
                .removePrefix("http://")
                .removePrefix("https://")
                .removeSuffix("/")
            return domain
        }
}
