package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.Person
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmStatic

object PersonConstants  {

    @JvmStatic
    val GENDER_MESSAGE_ID_MAP = mapOf(Person.GENDER_FEMALE to MessageID.female,
        Person.GENDER_MALE to MessageID.male,
        Person.GENDER_OTHER to MessageID.other)

    @JvmStatic
    val  CONNECTIVITY_STATUS_MAP = mapOf(Person.CONNECTIVITY_STATUS_BAD to MessageID.unset,
        Person.CONNECTIVITY_STATUS_MEDIUM to MessageID.unset,
        Person.CONNECTIVITY_STATUS_GOOD to MessageID.unset,
        Person.CONNECTIVITY_STATUS_NOT_TO_SAY to MessageID.unset)


}