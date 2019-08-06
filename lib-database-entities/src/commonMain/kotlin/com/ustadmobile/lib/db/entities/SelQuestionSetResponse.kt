package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

/**
 * Represents one run through of a question set for one particular student.
 */
@UmEntity(tableId = 27)
@Entity
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


    @UmSyncMasterChangeSeqNum
    var selQuestionSetResponseMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var selQuestionSetResponseLocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var selQuestionSetResponseLastChangedBy: Int = 0
}
