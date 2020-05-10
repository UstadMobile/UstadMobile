package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

class ClazzMemberWithClazz : ClazzMember() {

    @Embedded
    var clazz: Clazz? = null

}