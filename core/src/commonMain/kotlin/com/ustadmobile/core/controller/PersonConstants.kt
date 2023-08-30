package com.ustadmobile.core.controller

import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.Person
import kotlin.jvm.JvmStatic

object PersonConstants  {

    @JvmStatic
    val GENDER_MESSAGE_ID_MAP = mapOf(
        Person.GENDER_FEMALE to MR.strings.female,
        Person.GENDER_MALE to MR.strings.male,
        Person.GENDER_OTHER to MR.strings.other,
        Person.GENDER_UNSET to MR.strings.unset,
    )

}