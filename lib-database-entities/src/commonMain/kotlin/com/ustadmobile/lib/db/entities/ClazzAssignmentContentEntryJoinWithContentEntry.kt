package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
open class ClazzAssignmentContentEntryJoinWithContentEntry : ClazzAssignmentContentJoin(){

    @Embedded
    var contentEntry: ContentEntry? = null

}