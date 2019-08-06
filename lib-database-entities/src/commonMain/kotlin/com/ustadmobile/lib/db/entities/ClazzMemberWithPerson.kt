package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEmbedded

/**
 * POJO representing Person and ClazzMember
 */
class ClazzMemberWithPerson : ClazzMember() {

    @UmEmbedded
    var person: Person? = null
}
