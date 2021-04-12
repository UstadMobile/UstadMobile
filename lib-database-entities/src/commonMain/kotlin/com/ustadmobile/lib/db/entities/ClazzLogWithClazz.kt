package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

class ClazzLogWithClazz: ClazzLog() {

    @Embedded
    var clazz: Clazz? = null

}