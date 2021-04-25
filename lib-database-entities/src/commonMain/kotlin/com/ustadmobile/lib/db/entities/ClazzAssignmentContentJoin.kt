package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ClazzAssignmentContentJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID,
        notifyOnUpdate = [
            """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, $TABLE_ID AS tableId FROM 
        ChangeLog
        JOIN ClazzAssignmentContentJoin ON ChangeLog.chTableId = $TABLE_ID AND ClazzAssignmentContentJoin.assignmentContentJoinUid = ChangeLog.chEntityPk
        JOIN ClazzAssignment ON ClazzAssignment.assignmentUid = ClazzAssignmentContentJoin.assignmentContentJoinAssignmentUid
        JOIN Clazz ON Clazz.clazzUid = ClazzAssignment.assignmentClazzUid 
        JOIN Person ON Person.personUid IN (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1}  ${Role.PERMISSION_ASSIGNMENT_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid"""
        ],
        syncFindAllQuery = """
        SELECT ClazzAssignmentContentJoin.* FROM
        ClazzAssignmentContentJoin
        JOIN ClazzWork ON Assignment.assignmentUid = ClazzAssignmentContentJoin.assignmentContentJoinAssignmentUid
        JOIN Clazz ON Clazz.clazzUid = ClazzAssignment.assignmentClazzUid
        JOIN Person ON Person.personUid IN  (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1} ${Role.PERMISSION_ASSIGNMENT_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
        WHERE DeviceSession.dsDeviceId = :clientId""")
@Serializable
class ClazzAssignmentContentJoin {

    @PrimaryKey(autoGenerate = true)
    var assignmentContentJoinUid: Long = 0

    var assignmentContentJoinContentUid : Long = 0

    var assignmentContentJoinAssignmentUid : Long = 0

    var assignmentContentJoinActive : Boolean = true

    @MasterChangeSeqNum
    var assignmentContentJoinMCSN: Long = 0

    @LocalChangeSeqNum
    var assignmentContentJoinLCSN: Long = 0

    @LastChangedBy
    var assignmentContentJoinLCB: Int = 0

    @LastChangedTime
    var assignmentContentJoinLct: Long = 0

    companion object {

        const val TABLE_ID = 521

    }

}