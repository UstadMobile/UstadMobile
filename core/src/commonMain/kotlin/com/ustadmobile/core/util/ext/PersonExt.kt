package com.ustadmobile.core.util.ext

import com.soywiz.klock.DateTime
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.schedule.age
import com.ustadmobile.lib.db.entities.Person

fun Person.personFullName(): String = firstNames + " " + lastName

val Person.isMinor: Boolean
    get() = DateTime(dateOfBirth).age() < UstadMobileConstants.MINOR_AGE_THRESHOLD

