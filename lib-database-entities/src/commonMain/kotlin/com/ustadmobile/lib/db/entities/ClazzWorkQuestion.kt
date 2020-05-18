package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = 202)
@Entity
@Serializable
open class ClazzWorkQuestion {

    @PrimaryKey(autoGenerate = true)
    var clazzWorkQuestionUid: Long = 0

    var clazzWorkQuestionText: String? = null

    var clazzWorkQuestionClazzWorkUid: Long = 0

    var clazzWorkQuestionIndex: Int = 0

    var clazzWorkQuestionType: Int = 0

    var clazzWorkQuestionActive: Boolean = false

    @MasterChangeSeqNum
    var selQuestionMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var selQuestionLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var selQuestionLastChangedBy: Int = 0

    companion object{
        const val CLAZZ_WORK_QUESTION_TYPE_FREE_TEXT = 1
        const val CLAZZ_WORK_QUESTION_TYPE_MULTIPLE_CHOICE = 2
    }
}
