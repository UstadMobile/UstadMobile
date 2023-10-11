package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount

fun Person.personFullName(): String = "${firstNames ?: ""} ${lastName ?: ""}"

fun Person.initials(): String {
    return (firstNames?.initials() ?: "") + " " + (lastName?.initials() ?: "")
}

fun Person.toUmAccount(endpointUrl: String) = UmAccount(personUid = personUid,
    username = username, auth = "", endpointUrl = endpointUrl, firstName = firstNames,
    lastName = lastName, admin = admin)

