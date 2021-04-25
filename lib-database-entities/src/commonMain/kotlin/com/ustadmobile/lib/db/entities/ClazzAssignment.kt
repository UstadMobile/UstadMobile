package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ClazzAssignment.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID,
        notifyOnUpdate = [
            """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, $TABLE_ID AS tableId FROM 
        ChangeLog
        JOIN ClazzAssignment ON ChangeLog.chTableId = $TABLE_ID AND ClazzAssignment.clazzAssignmentUid = ChangeLog.chEntityPk
        JOIN Clazz ON Clazz.clazzUid = ClazzAssignment.clazzAssignmentClazzUid 
        JOIN Person ON Person.personUid IN (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1}  ${Role.PERMISSION_ASSIGNMENT_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
        """
        ],
        syncFindAllQuery = """
        SELECT Assignment.* FROM
        Assignment
        JOIN Clazz ON Clazz.clazzUid = ClazzAssignment.clazzAssignmentClazzUid
        JOIN Person ON Person.personUid IN  (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1} ${Role.PERMISSION_ASSIGNMENT_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
        WHERE DeviceSession.dsDeviceId = :clientId  
    """)
@Serializable
class ClazzAssignment {

    @PrimaryKey(autoGenerate = true)
    var clazzAssignmentUid: Long = 0

    var clazzAssignmentTitle: String? = null

    var clazzAssignmentDescription: String? = null

    var clazzAssignmentDeadlineDate: Long = 0

    var clazzAssignmentDeadlineDateTime: Long = 0

    var clazzAssignmentStartDate: Long = 0

    var clazzAssignmentStartDateTime: Long = 0

    var clazzAssignmentLateSubmissionType: Int = 0

    var clazzAssignmentLateSubmissionPenalty: Int = 0

    var clazzAssignmentGracePeriodDate: Long = 0

    var clazzAssignmentGracePeriodDateTime: Long = 0

    var clazzAssignmentActive: Boolean = true

    var clazzAssignmentClassCommentEnabled: Boolean = true

    var clazzAssignmentPrivateCommentsEnabled: Boolean = false

    var clazzAssignmentClazzUid: Long = 0

    @LocalChangeSeqNum
    var clazzAssignmentLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var clazzAssignmentMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzAssignmentLastChangedBy: Int = 0

    @LastChangedTime
    var clazzAssignmentLct: Long = 0

    companion object {

        const val TABLE_ID = 520

        const val ASSIGNMENT_LATE_SUBMISSION_REJECT = 1
        const val ASSIGNMENT_LATE_SUBMISSION_PENALTY = 2
        const val ASSIGNMENT_LATE_SUBMISSION_ACCEPT = 3
    }


}