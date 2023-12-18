package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.Person

object PersonConstants {

    val GENDER_MESSAGE_IDS = listOf(
        MessageIdOption2(MR.strings.female, Person.GENDER_FEMALE),
        MessageIdOption2(MR.strings.male, Person.GENDER_MALE),
        MessageIdOption2(MR.strings.other, Person.GENDER_OTHER),
    )

    val GENDER_MESSAGE_IDS_AND_UNSET =
        listOf(MessageIdOption2(MR.strings.unset, Person.GENDER_UNSET)) + GENDER_MESSAGE_IDS

}