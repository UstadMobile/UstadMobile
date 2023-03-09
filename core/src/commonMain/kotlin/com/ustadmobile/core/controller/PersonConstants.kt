package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.Person
import kotlin.jvm.JvmStatic

object PersonConstants  {

    @JvmStatic
    val GENDER_MESSAGE_ID_MAP = mapOf(
        Person.GENDER_FEMALE to MessageID.female,
        Person.GENDER_MALE to MessageID.male,
        Person.GENDER_OTHER to MessageID.other,
        Person.GENDER_UNSET to MessageID.unset,
    )

}