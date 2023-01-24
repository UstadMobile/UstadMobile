package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.Role

object PermissionConstants {

    val PERMISSION_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.permission_person_delegate, Role.PERMISSION_PERSON_DELEGATE.toInt()),
    )

}