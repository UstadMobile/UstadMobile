package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.xapi.model.XapiAccount
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount

fun Person.personFullName(): String = "${firstNames ?: ""} ${lastName ?: ""}"

fun Person.initials(): String {
    return (firstNames?.initials() ?: "") + " " + (lastName?.initials() ?: "")
}

fun Person.toUmAccount(endpointUrl: String) = UmAccount(personUid = personUid,
    username = username, auth = "", endpointUrl = endpointUrl, firstName = firstNames,
    lastName = lastName, admin = admin)


fun Person.isGuestUser() : Boolean {
    return username == null
}

fun Person.toXapiAgent(endpoint: Endpoint): XapiAgent {
    return XapiAgent(
        name = personFullName(),
        account = XapiAccount(
            name = username ?: throw IllegalArgumentException("Cannot make an XapiAgent for null username"),
            homePage = endpoint.url,
        )
    )
}
