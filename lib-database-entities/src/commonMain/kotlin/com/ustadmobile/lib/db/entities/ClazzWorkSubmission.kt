package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = 206)
@Serializable
open class ClazzWorkSubmission() {

    @PrimaryKey(autoGenerate = true)
    var clazzWorkSubmissionUid: Long = 0

    var clazzWorkSubmissionClazzWorkUid : Long = 0

    var clazzWorkSubmissionClazzMemberUid : Long = 0

    var clazzWorkSubmissionMarkerClazzMemberUid: Long = 0

    var clazzWorkSubmissionMarkerPersonUid: Long = 0

    var clazzWorkSubmissionPersonUid: Long = 0

    var clazzWorkSubmissionInactive : Boolean = false

    var clazzWorkSubmissionDateTimeStarted : Long = 0

    var clazzWorkSubmissionDateTimeUpdated: Long = 0

    var clazzWorkSubmissionDateTimeFinished: Long = 0

    var clazzWorkSubmissionDateTimeMarked: Long = 0

    var clazzWorkSubmissionText : String? = null

    var clazzWorkSubmissionScore: Int = 0


    @MasterChangeSeqNum
    var clazzWorkSubmissionMCSN: Long = 0

    @LocalChangeSeqNum
    var clazzWorkSubmissionLCSN: Long = 0

    @LastChangedBy
    var clazzWorkSubmissionLCB: Int = 0


}
