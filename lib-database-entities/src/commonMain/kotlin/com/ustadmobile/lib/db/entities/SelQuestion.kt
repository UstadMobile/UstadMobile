package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

/**
 * Represents the question within a question set.
 * eg: "Select the students who sit alone"
 *
 */
@SyncableEntity(tableId = 22)
@Entity
@Serializable
class SelQuestion {

    @PrimaryKey(autoGenerate = true)
    var selQuestionUid: Long = 0

    var questionText: String? = null

    // -> SelQuestionSet - what set is this question a part of
    var selQuestionSelQuestionSetUid: Long = 0

    //The order.
    var questionIndex: Int = 0

    //If this question is to be assigned to all classes. (if not - not handled / implemented yet).
    var assignToAllClasses: Boolean = false

    //If this question allows for multiple nominations.
    var multiNominations: Boolean = false

    var questionType: Int = 0

    var questionActive: Boolean = false

    @MasterChangeSeqNum
    var selQuestionMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var selQuestionLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var selQuestionLastChangedBy: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SelQuestion

        if (selQuestionUid != other.selQuestionUid) return false
        if (questionText != other.questionText) return false
        if (selQuestionSelQuestionSetUid != other.selQuestionSelQuestionSetUid) return false
        if (questionIndex != other.questionIndex) return false
        if (assignToAllClasses != other.assignToAllClasses) return false
        if (multiNominations != other.multiNominations) return false
        if (questionType != other.questionType) return false
        if (questionActive != other.questionActive) return false
        if (selQuestionMasterChangeSeqNum != other.selQuestionMasterChangeSeqNum) return false
        if (selQuestionLocalChangeSeqNum != other.selQuestionLocalChangeSeqNum) return false
        if (selQuestionLastChangedBy != other.selQuestionLastChangedBy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = selQuestionUid.hashCode()
        result = 31 * result + (questionText?.hashCode() ?: 0)
        result = 31 * result + selQuestionSelQuestionSetUid.hashCode()
        result = 31 * result + questionIndex
        result = 31 * result + assignToAllClasses.hashCode()
        result = 31 * result + multiNominations.hashCode()
        result = 31 * result + questionType
        result = 31 * result + questionActive.hashCode()
        result = 31 * result + selQuestionMasterChangeSeqNum.hashCode()
        result = 31 * result + selQuestionLocalChangeSeqNum.hashCode()
        result = 31 * result + selQuestionLastChangedBy
        return result
    }


}
