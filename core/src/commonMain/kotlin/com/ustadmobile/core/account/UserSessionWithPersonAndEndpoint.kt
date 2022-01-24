package com.ustadmobile.core.account

import com.ustadmobile.core.util.ext.toUmAccount
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UserSession
import kotlinx.serialization.Serializable

@Serializable
data class UserSessionWithPersonAndEndpoint (val userSession: UserSession,
                                             val person: Person,
                                             val endpoint: Endpoint
) {

    fun toUmAccount() = person.toUmAccount(endpoint.url)

}