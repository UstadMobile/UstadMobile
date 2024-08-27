package com.ustadmobile.core.account

import com.ustadmobile.core.domain.xapi.model.XapiAccount
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.util.ext.toUmAccount
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.UserSession
import kotlinx.serialization.Serializable

@Serializable
data class UserSessionWithPersonAndLearningSpace (
    val userSession: UserSession,
    val person: Person,
    val learningSpace: LearningSpace=LearningSpace("http://192.168.1.52:8087/"),
    val personPicture: PersonPicture? = null,
) {

    val displayName: String
        get() {
            val displayUrl = learningSpace.url
                .removePrefix("http://")
                .removePrefix("https://")
                .removeSuffix("/")
            return "${person.username}@$displayUrl"
        }

    fun toUmAccount() = person.toUmAccount(learningSpace.url)

    fun toXapiAgent() = XapiAgent(
        account = XapiAccount(
            homePage = learningSpace.url,
            name = person.username ?: "anonymous",
        )
    )


}