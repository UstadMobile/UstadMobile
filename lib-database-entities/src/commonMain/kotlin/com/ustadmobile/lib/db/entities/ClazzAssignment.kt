package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.School.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID)
@Serializable
open class ClazzAssignment() {

    @PrimaryKey(autoGenerate = true)
    var clazzAssignmentUid: Long = 0

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
}
