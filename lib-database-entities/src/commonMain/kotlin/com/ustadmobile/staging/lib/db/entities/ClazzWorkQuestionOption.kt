package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = 203)
@Entity
@Serializable
open class ClazzWorkQuestionOption {

    @PrimaryKey(autoGenerate = true)
    var clazzWorkQuestionOptionUid: Long = 0

    var clazzWorkQuestionOptionText: String? = null

    var clazzWorkQuestionOptionQuestionUid: Long = 0

    @MasterChangeSeqNum
    var clazzWorkQuestionOptionMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var clazzWorkQuestionOptionLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzWorkQuestionOptionLastChangedBy: Int = 0

    var clazzWorkQuestionOptionActive: Boolean = false



}
