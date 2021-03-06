package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ClazzWork.Companion.CLAZZ_WORK_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = CLAZZ_WORK_TABLE_ID)
@Serializable
open class ClazzWork {

    @PrimaryKey(autoGenerate = true)
    var clazzWorkUid: Long = 0

    var clazzWorkCreatorPersonUid: Long = 0

    var clazzWorkClazzUid : Long = 0

    var clazzWorkTitle : String? = null

    var clazzWorkCreatedDate: Long = 0

    var clazzWorkStartDateTime: Long = 0

    var clazzWorkStartTime: Long = 0

    var clazzWorkDueTime: Long = 0

    var clazzWorkDueDateTime: Long = Long.MAX_VALUE

    var clazzWorkSubmissionType: Int = 0

    var clazzWorkCommentsEnabled : Boolean = false

    var clazzWorkMaximumScore: Int = 0

    var clazzWorkInstructions : String? = null

    var clazzWorkActive: Boolean = true

    @LocalChangeSeqNum
    var clazzWorkLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var clazzWorkMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzWorkLastChangedBy: Int = 0

    @LastChangedTime
    var clazzWorkLct: Long = 0


    constructor(){
        clazzWorkActive = true
    }

    companion object{
        const val CLAZZ_WORK_TABLE_ID = 201
        const val CLAZZ_WORK_SUBMISSION_TYPE_NONE = 0
        const val CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT = 1
        const val CLAZZ_WORK_SUBMISSION_TYPE_ATTACHMENT = 2
        const val CLAZZ_WORK_SUBMISSION_TYPE_QUIZ = 3
    }


}
