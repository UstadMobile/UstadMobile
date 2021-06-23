package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = ClazzWorkQuestionOption.TABLE_ID)
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

    @LastChangedTime
    var clazzWorkQuestionOptionLct: Long = 0

    var clazzWorkQuestionOptionActive: Boolean = false
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ClazzWorkQuestionOption

        if (clazzWorkQuestionOptionUid != other.clazzWorkQuestionOptionUid) return false
        if (clazzWorkQuestionOptionText != other.clazzWorkQuestionOptionText) return false
        if (clazzWorkQuestionOptionQuestionUid != other.clazzWorkQuestionOptionQuestionUid) return false
        if (clazzWorkQuestionOptionMasterChangeSeqNum != other.clazzWorkQuestionOptionMasterChangeSeqNum) return false
        if (clazzWorkQuestionOptionLocalChangeSeqNum != other.clazzWorkQuestionOptionLocalChangeSeqNum) return false
        if (clazzWorkQuestionOptionLastChangedBy != other.clazzWorkQuestionOptionLastChangedBy) return false
        if (clazzWorkQuestionOptionActive != other.clazzWorkQuestionOptionActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazzWorkQuestionOptionUid.hashCode()
        result = 31 * result + (clazzWorkQuestionOptionText?.hashCode() ?: 0)
        result = 31 * result + clazzWorkQuestionOptionQuestionUid.hashCode()
        result = 31 * result + clazzWorkQuestionOptionMasterChangeSeqNum.hashCode()
        result = 31 * result + clazzWorkQuestionOptionLocalChangeSeqNum.hashCode()
        result = 31 * result + clazzWorkQuestionOptionLastChangedBy
        result = 31 * result + clazzWorkQuestionOptionActive.hashCode()
        return result
    }

    companion object {
        const val TABLE_ID = 203
    }


}
