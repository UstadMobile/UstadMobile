package com.ustadmobile.core.account

import com.ustadmobile.core.domain.xapi.model.XapiAccount
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.util.ext.toUmAccount
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.UserSession
import kotlinx.serialization.Serializable

@Serializable
data class UserSessionWithPersonAndEndpoint (
    val userSession: UserSession,
    val person: Person,
    val endpoint: Endpoint,
    val personPicture: PersonPicture? = null,
) {

    val displayName: String
        get() {
            val displayUrl = endpoint.url
                .removePrefix("http://")
                .removePrefix("https://")
                .removeSuffix("/")
            return "${person.username}@$displayUrl"
        }

    fun toUmAccount() = person.toUmAccount(endpoint.url)

    fun toXapiAgent() = XapiAgent(
        account = XapiAccount(
            homePage = endpoint.url,
            name = person.username ?: "anonymous",
        )
    )


}