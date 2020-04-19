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
open class SelQuestionOption {

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
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SelQuestionOption

        if (selQuestionOptionUid != other.selQuestionOptionUid) return false
        if (optionText != other.optionText) return false
        if (selQuestionOptionQuestionUid != other.selQuestionOptionQuestionUid) return false
        if (selQuestionOptionMasterChangeSeqNum != other.selQuestionOptionMasterChangeSeqNum) return false
        if (selQuestionOptionLocalChangeSeqNum != other.selQuestionOptionLocalChangeSeqNum) return false
        if (selQuestionOptionLastChangedBy != other.selQuestionOptionLastChangedBy) return false
        if (optionActive != other.optionActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = selQuestionOptionUid.hashCode()
        result = 31 * result + (optionText?.hashCode() ?: 0)
        result = 31 * result + selQuestionOptionQuestionUid.hashCode()
        result = 31 * result + selQuestionOptionMasterChangeSeqNum.hashCode()
        result = 31 * result + selQuestionOptionLocalChangeSeqNum.hashCode()
        result = 31 * result + selQuestionOptionLastChangedBy
        result = 31 * result + optionActive.hashCode()
        return result
    }


}
