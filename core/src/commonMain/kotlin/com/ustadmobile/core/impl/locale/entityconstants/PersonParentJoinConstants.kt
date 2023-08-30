package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin

object PersonParentJoinConstants {

    val RELATIONSHIP_MESSAGE_IDS = listOf(
        MessageIdOption2(MR.strings.mother, PersonParentJoin.RELATIONSHIP_MOTHER),
        MessageIdOption2(MR.strings.father, PersonParentJoin.RELATIONSHIP_FATHER),
        MessageIdOption2(MR.strings.other_legal_guardian, PersonParentJoin.RELATIONSHIP_OTHER)
    )
}