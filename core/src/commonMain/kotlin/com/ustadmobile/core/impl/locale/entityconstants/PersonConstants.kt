package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.Person

object PersonConstants {

    val GENDER_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.female, Person.GENDER_FEMALE),
        MessageIdOption2(MessageID.male, Person.GENDER_MALE),
        MessageIdOption2(MessageID.other, Person.GENDER_OTHER),
    )

}