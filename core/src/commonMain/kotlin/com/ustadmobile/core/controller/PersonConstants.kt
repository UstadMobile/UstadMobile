package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonConnectivity
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmStatic

object PersonConstants  {

    @JvmStatic
    val GENDER_MESSAGE_ID_MAP = mapOf(Person.GENDER_FEMALE to MessageID.female,
        Person.GENDER_MALE to MessageID.male,
        Person.GENDER_OTHER to MessageID.other)

    @JvmStatic
    val  CONNECTIVITY_STATUS_MAP = mapOf(PersonConnectivity.CONNECTIVITY_STATUS_NONE to MessageID.None,
        PersonConnectivity.CONNECTIVITY_STATUS_LIMIT to MessageID.connectivity_limited,
        PersonConnectivity.CONNECTIVITY_STATUS_FULL to MessageID.connectivity_full,
        PersonConnectivity.CONNECTIVITY_STATUS_NOT_TO_SAY to MessageID.prefer_not_to_say)


}