package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = 201)
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

    var clazzWorkDueDateTime: Long = 0

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


    constructor(){
        clazzWorkActive = true
    }

    companion object{
        const val CLAZZ_WORK_SUBMISSION_TYPE_NONE = 0
        const val CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT = 1
        const val CLAZZ_WORK_SUBMISSION_TYPE_ATTACHMENT = 2
        const val CLAZZ_WORK_SUBMISSION_TYPE_QUIZ = 3
    }


}
