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
open class ClazzWorkQuestionResponse {

    @PrimaryKey(autoGenerate = true)
    var clazzWorkQuestionResponseUid: Long = 0

    var clazzWorkQuestionResponseText: String? = null

    var clazzWorkQuestionResponseClazzWorkUid: Long = 0

    var clazzWorkQuestionResponseOptionSelected: Long = 0


}
