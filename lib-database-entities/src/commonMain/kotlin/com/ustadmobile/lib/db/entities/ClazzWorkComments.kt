package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.ClazzAssignmentContentJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = 205)
@Serializable
open class ClazzWorkComments() {

    @PrimaryKey(autoGenerate = true)
    var clazzWorkCommentsUid: Long = 0

    var clazzWorkCommentsClazzWorkUid : Long = 0

    var clazzWorkCommentsClazzMemberUid : Long = 0

    var clazzWorkCommentsPersonUid : Long = 0

    var clazzWorkCommentsInactive : Boolean = false

    var clazzWorkCommentsDateTimeAdded : Long = 0

    var clazzWorkCommentsDateTimeUpdated: Long = 0

    var clazzWorkCommentsPublic: Boolean = false

    @MasterChangeSeqNum
    var clazzWorkCommentsMCSN: Long = 0

    @LocalChangeSeqNum
    var clazzWorkCommentsLCSN: Long = 0

    @LastChangedBy
    var clazzWorkCommentsLCB: Int = 0


}
