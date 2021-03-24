package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.Person

fun Person.personFullName(): String = "$firstNames $lastName"

