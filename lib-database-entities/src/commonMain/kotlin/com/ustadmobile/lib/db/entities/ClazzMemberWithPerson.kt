package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

/**
 * POJO representing Person and ClazzMember
 */
class ClazzMemberWithPerson : ClazzMember() {

    @Embedded
    var person: Person? = null
}
