package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable


/**
 * Represents a set of social nomination question eg: "Question set for Region A"
 */
@SyncableEntity(tableId = 25)
@Entity
@Serializable
open class SelQuestionSet {

    @PrimaryKey(autoGenerate = true)
    var selQuestionSetUid: Long = 0

    // The set title.
    var title: String? = null

    @MasterChangeSeqNum
    var selQuestionSetMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var selQuestionSetLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var selQuestionSetLastChangedBy: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SelQuestionSet

        if (selQuestionSetUid != other.selQuestionSetUid) return false
        if (title != other.title) return false
        if (selQuestionSetMasterChangeSeqNum != other.selQuestionSetMasterChangeSeqNum) return false
        if (selQuestionSetLocalChangeSeqNum != other.selQuestionSetLocalChangeSeqNum) return false
        if (selQuestionSetLastChangedBy != other.selQuestionSetLastChangedBy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = selQuestionSetUid.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + selQuestionSetMasterChangeSeqNum.hashCode()
        result = 31 * result + selQuestionSetLocalChangeSeqNum.hashCode()
        result = 31 * result + selQuestionSetLastChangedBy
        return result
    }


}
