package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Assignment.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID,
        notifyOnUpdate = [
            """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, $TABLE_ID AS tableId FROM 
        ChangeLog
        JOIN Assignment ON ChangeLog.chTableId = $TABLE_ID AND Assignment.assignmentUid = ChangeLog.chEntityPk
        JOIN Clazz ON Clazz.clazzUid = Assignment.assignmentClazzUid 
        JOIN Person ON Person.personUid IN (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1}  ${Role.PERMISSION_ASSIGNMENT_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
        """
        ],
        syncFindAllQuery = """
        SELECT Assignment.* FROM
        Assignment
        JOIN Clazz ON Clazz.clazzUid = Assignment.assignmentClazzUid
        JOIN Person ON Person.personUid IN  (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1} ${Role.PERMISSION_ASSIGNMENT_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
        WHERE DeviceSession.dsDeviceId = :clientId  
    """)
@Serializable
class Assignment {

    @PrimaryKey(autoGenerate = true)
    var assignmentUid: Long = 0

    var assignmentTitle: String? = null

    var assignmentDeadlineDate: Long = 0

    var assignmentDeadlineDateTime: Long = 0

    var assignmentStartDate: Long = 0

    var assignmentStartDateTime: Long = 0

    var assignmentLateSubmissionType: Int = 0

    var assignmentLateSubmissionPenalty: Int = 0

    var assignmentGracePeriodDate: Long = 0

    var assignmentGracePeriodDateTime: Long = 0

    var assignmentActive: Boolean = true

    var assignmentClassCommentEnabled: Boolean = true

    var assignmentPrivateCommentsEnabled: Boolean = false

    var assignmentClazzUid: Long = 0

    @LocalChangeSeqNum
    var assignmentLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var assignmentMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var assignmentLastChangedBy: Int = 0

    @LastChangedTime
    var assignmentLct: Long = 0

    companion object {

        const val TABLE_ID = 520

        const val ASSIGNMENT_LATE_SUBMISSION_REJECT = 1
        const val ASSIGNMENT_LATE_SUBMISSION_PENALTY = 2
        const val ASSIGNMENT_LATE_SUBMISSION_ACCEPT = 3
    }


}