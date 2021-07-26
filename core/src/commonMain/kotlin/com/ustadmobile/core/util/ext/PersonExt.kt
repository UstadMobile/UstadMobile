package com.ustadmobile.core.util.ext

import com.soywiz.klock.DateTime
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.schedule.age
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount

fun Person.personFullName(): String = "$firstNames $lastName"

fun Person.toUmAccount(endpointUrl: String) = UmAccount(personUid = personUid,
    username = username, auth = "", endpointUrl = endpointUrl, firstName = firstNames,
    lastName = lastName, admin = admin)

val Person.isMinor: Boolean
    get() = DateTime(dateOfBirth).age() < UstadMobileConstants.MINOR_AGE_THRESHOLD

