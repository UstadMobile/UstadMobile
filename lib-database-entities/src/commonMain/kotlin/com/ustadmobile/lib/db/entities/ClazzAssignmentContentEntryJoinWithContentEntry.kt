package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

open class ClazzAssignmentContentEntryJoinWithContentEntry : ClazzAssignmentContentJoin(){

    @Embedded
    var contentEntry: ContentEntry? = null

}