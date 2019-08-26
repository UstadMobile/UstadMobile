package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import com.ustadmobile.lib.database.annotation.UmEmbedded

/**
 * POJO representing Person and ClazzMember
 */
class ClazzMemberWithPerson : ClazzMember() {

    @UmEmbedded
    @Embedded
    var person: Person? = null
}
