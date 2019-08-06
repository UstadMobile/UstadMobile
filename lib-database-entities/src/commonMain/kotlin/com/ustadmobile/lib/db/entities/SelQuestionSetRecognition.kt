package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

/**
 * FOR RECOGNITION
 *
 * For each run through of a question set for one student, they must attempt to recognize their
 * classmates.
 * There is 1:many relationship between this entity and SelQuestionSetResponse.
 * There is one SelQuestionSetRecognition for each
 * SelQuestionSetResponse for each student in the class..
 *
 */
@UmEntity(tableId = 26)
@Entity
class SelQuestionSetRecognition {

    @PrimaryKey(autoGenerate = true)
    var selQuestionSetRecognitionUid: Long = 0

    // -> SelQuestionSetResponse - The question set response (which has recognition percentages)
    var selQuestionSetRecognitionSelQuestionSetResponseUid: Long = 0

    // The Clazz Member - The Student To BE recognized.
    var selQuestionSetRecognitionClazzMemberUid: Long = 0

    // Boolean if recognized or not by the ClazzMember doing this QuestionSet in QuestionSetResponse.
    var isSelQuestionSetRecognitionRecognized: Boolean = false

    @UmSyncMasterChangeSeqNum
    var selQuestionSetRecognitionMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var selQuestionSetRecognitionLocalChangeSeqNum: Long = 0


    @UmSyncLastChangedBy
    var selQuestionSetRecognitionLastChangedBy: Int = 0
}
