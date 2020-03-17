package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = 52)
@Entity
@Serializable
class SelQuestionOption {

    @PrimaryKey(autoGenerate = true)
    var selQuestionOptionUid: Long = 0

    var optionText: String? = null

    var selQuestionOptionQuestionUid: Long = 0

    @MasterChangeSeqNum
    var selQuestionOptionMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var selQuestionOptionLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var selQuestionOptionLastChangedBy: Int = 0

    var optionActive: Boolean = false
}
