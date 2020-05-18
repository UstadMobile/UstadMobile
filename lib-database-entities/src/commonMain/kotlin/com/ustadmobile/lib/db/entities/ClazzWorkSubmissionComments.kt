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
@SyncableEntity(tableId = 207)
@Serializable
open class ClazzWorkSubmissionComments() {

    @PrimaryKey(autoGenerate = true)
    var clazzWorkSubmissionCommentsUid: Long = 0

    var clazzWorkSubmissionCommentsClazzWorkUid : Long = 0

    var clazzWorkSubmissionCommentsClazzWorkSubmissionUid: Long = 0

    //eg: The clazz member commenting..
    var clazzWorkSubmissionCommentsClazzMemberUid : Long = 0

    //..or .. The person commenting. (Principal/parent not a clazz member?)
    var clazzWorkSubmissionCommentsPersonUid : Long = 0

    var clazzWorkSubmissionCommentsInactive : Boolean = false

    var clazzWorkSubmissionCommentsDateTimeAdded : Long = 0

    var clazzWorkSubmissionCommentsDateTimeUpdated: Long = 0

    @MasterChangeSeqNum
    var clazzWorkSubmissionCommentsMCSN: Long = 0

    @LocalChangeSeqNum
    var clazzWorkSubmissionCommentsLCSN: Long = 0

    @LastChangedBy
    var clazzWorkSubmissionCommentsLCB: Int = 0


}
