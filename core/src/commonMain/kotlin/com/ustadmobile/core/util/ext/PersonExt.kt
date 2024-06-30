package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.xapi.model.XapiAccount
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount

fun Person.personFullName(): String = "${firstNames ?: ""} ${lastName ?: ""}"

fun Person.toUmAccount(endpointUrl: String) = UmAccount(personUid = personUid,
    username = username, auth = "", endpointUrl = endpointUrl, firstName = firstNames,
    lastName = lastName)


fun Person.isGuestUser() : Boolean {
    return username == null
}

/**
 * Converts a Person to an XapiAgent
 * name - full name e.g. first names last name
 * account - homepage is endpoint.url
 * account name is the personuid.
 */
fun Person.toXapiAgent(endpoint: Endpoint): XapiAgent {
    return XapiAgent(
        name = personFullName(),
        account = XapiAccount(
            name = username ?: personUid.toString(),
            homePage = endpoint.url,
        )
    )
}
