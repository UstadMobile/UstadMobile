package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

/**
 * Represents one run through of a question set for one particular student.
 */
@SyncableEntity(tableId = 27)
@Entity
@Serializable
class SelQuestionSetResponse {

    @PrimaryKey(autoGenerate = true)
    var selQuestionSetResposeUid: Long = 0

    //-> SelQuestionSet - The Question Set
    var selQuestionSetResponseSelQuestionSetUid: Long = 0

    //clazz member doing this - The student (Class Member) doing this.
    var selQuestionSetResponseClazzMemberUid: Long = 0

    //start time
    var selQuestionSetResponseStartTime: Long = 0

    //finish time
    var selQuestionSetResponseFinishTime: Long = 0

    //total Response Recognition percentage. - to be calculated on device (not database).
    var selQuestionSetResponseRecognitionPercentage: Float = 0.toFloat()

    @MasterChangeSeqNum
    var selQuestionSetResponseMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var selQuestionSetResponseLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var selQuestionSetResponseLastChangedBy: Int = 0
}
