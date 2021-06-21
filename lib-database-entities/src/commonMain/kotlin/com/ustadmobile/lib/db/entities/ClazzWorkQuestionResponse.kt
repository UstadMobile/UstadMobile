package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = ClazzWorkQuestionResponse.TABLE_ID)
@Entity
@Serializable
open class ClazzWorkQuestionResponse {

    @PrimaryKey(autoGenerate = true)
    var clazzWorkQuestionResponseUid: Long = 0

    //Might not need it as we already have QuestionUid
    var clazzWorkQuestionResponseClazzWorkUid: Long = 0

    var clazzWorkQuestionResponseQuestionUid : Long = 0

    // for answers that are text
    var clazzWorkQuestionResponseText: String? = null

    //for answers that are quiz option the option uid
    var clazzWorkQuestionResponseOptionSelected: Long = 0

    var clazzWorkQuestionResponsePersonUid: Long = 0

    var clazzWorkQuestionResponseInactive: Boolean = false

    var clazzWorkQuestionResponseDateResponded: Long = 0

    @MasterChangeSeqNum
    var clazzWorkQuestionResponseMCSN: Long = 0

    @LocalChangeSeqNum
    var clazzWorkQuestionResponseLCSN: Long = 0

    @LastChangedBy
    var clazzWorkQuestionResponseLCB: Int = 0

    @LastChangedTime
    var clazzWorkQuestionResponseLct: Long = 0

    companion object {
        const val TABLE_ID = 209
    }
}
