package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.AssignmentContentJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID,
        notifyOnUpdate = [
            """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, $TABLE_ID AS tableId FROM 
        ChangeLog
        JOIN AssignmentContentJoin ON ChangeLog.chTableId = $TABLE_ID AND AssignmentContentJoin.assignmentContentJoinUid = ChangeLog.chEntityPk
        JOIN Assignment ON Assignment.assignmentUid = AssignmentContentJoin.assignmentContentJoinAssignmentUid
        JOIN Clazz ON Clazz.clazzUid = Assignment.assignmentClazzUid 
        JOIN Person ON Person.personUid IN (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1}  ${Role.PERMISSION_ASSIGNMENT_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid"""
        ],
        syncFindAllQuery = """
        SELECT AssignmentContentJoin.* FROM
        AssignmentContentJoin
        JOIN ClazzWork ON Assignment.assignmentUid = AssignmentContentJoin.assignmentContentJoinAssignmentUid
        JOIN Clazz ON Clazz.clazzUid = Assignment.assignmentClazzUid
        JOIN Person ON Person.personUid IN  (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1} ${Role.PERMISSION_ASSIGNMENT_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
        WHERE DeviceSession.dsDeviceId = :clientId""")
@Serializable
class AssignmentContentJoin {

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