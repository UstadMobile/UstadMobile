package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.ClazzAssignment.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID)
@Serializable
open class ClazzAssignment() {

    @PrimaryKey(autoGenerate = true)
    var clazzAssignmentUid: Long = 0

    var clazzAssignmentTitle : String ?= null

    //The clazz
    var clazzAssignmentClazzUid : Long = 0

    //Inactive flag. Default to false (everything that persists will be active & shown)
    var clazzAssignmentInactive : Boolean = false

    var clazzAssignmentStartDate : Long = 0

    var clazzAssignmentDueDate : Long = 0

    var clazzAssignmentCreationDate: Long = 0

    var clazzAssignmentUpdateDate : Long = 0

    var clazzAssignmentInstructions : String? = null

    var clazzAssignmentGrading : Int = 0

    var clazzAssignmentRequireAttachment : Boolean = false

    @MasterChangeSeqNum
    var clazzAssignmentMCSN: Long = 0

    @LocalChangeSeqNum
    var clazzAssignmentLCSN: Long = 0

    @LastChangedBy
    var clazzAssignmentLCB: Int = 0

    companion object {

        const val TABLE_ID = 176
        const val CLAZZ_ASSIGNMENT_GRADING_NONE = 0
        const val CLAZZ_ASSIGNMENT_GRADING_NUMERICAL = 1
        const val CLAZZ_ASSIGNMENT_GRADING_LETTERS = 2
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ClazzAssignment

        if (clazzAssignmentUid != other.clazzAssignmentUid) return false
        if (clazzAssignmentTitle != other.clazzAssignmentTitle) return false
        if (clazzAssignmentClazzUid != other.clazzAssignmentClazzUid) return false
        if (clazzAssignmentInactive != other.clazzAssignmentInactive) return false
        if (clazzAssignmentStartDate != other.clazzAssignmentStartDate) return false
        if (clazzAssignmentDueDate != other.clazzAssignmentDueDate) return false
        if (clazzAssignmentCreationDate != other.clazzAssignmentCreationDate) return false
        if (clazzAssignmentUpdateDate != other.clazzAssignmentUpdateDate) return false
        if (clazzAssignmentInstructions != other.clazzAssignmentInstructions) return false
        if (clazzAssignmentGrading != other.clazzAssignmentGrading) return false
        if (clazzAssignmentRequireAttachment != other.clazzAssignmentRequireAttachment) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazzAssignmentUid.hashCode()
        result = 31 * result + (clazzAssignmentTitle?.hashCode() ?: 0)
        result = 31 * result + clazzAssignmentClazzUid.hashCode()
        result = 31 * result + clazzAssignmentInactive.hashCode()
        result = 31 * result + clazzAssignmentStartDate.hashCode()
        result = 31 * result + clazzAssignmentDueDate.hashCode()
        result = 31 * result + clazzAssignmentCreationDate.hashCode()
        result = 31 * result + clazzAssignmentUpdateDate.hashCode()
        result = 31 * result + (clazzAssignmentInstructions?.hashCode() ?: 0)
        result = 31 * result + clazzAssignmentGrading
        result = 31 * result + clazzAssignmentRequireAttachment.hashCode()
        return result
    }


}
