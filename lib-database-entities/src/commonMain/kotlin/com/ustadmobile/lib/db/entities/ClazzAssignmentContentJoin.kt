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
        JOIN ClazzAssignmentContentJoin ON ChangeLog.chTableId = $TABLE_ID AND ClazzAssignmentContentJoin.clazzAssignmentContentJoinUid = ChangeLog.chEntityPk
        JOIN ClazzAssignment ON ClazzAssignment.clazzAssignmentUid = ClazzAssignmentContentJoin.clazzAssignmentContentJoinAssignmentUid
        JOIN Clazz ON Clazz.clazzUid = ClazzAssignment.clazzAssignmentClazzUid 
        JOIN Person ON Person.personUid IN (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1}  ${Role.PERMISSION_ASSIGNMENT_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid"""
        ],
        syncFindAllQuery = """
        SELECT ClazzAssignmentContentJoin.* FROM
        ClazzAssignmentContentJoin
        JOIN ClazzAssignment ON ClazzAssignment.clazzAssignmentUid = ClazzAssignmentContentJoin.clazzAssignmentContentJoinAssignmentUid
        JOIN Clazz ON Clazz.clazzUid = ClazzAssignment.clazzAssignmentClazzUid
        JOIN Person ON Person.personUid IN  (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1} ${Role.PERMISSION_ASSIGNMENT_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
        WHERE DeviceSession.dsDeviceId = :clientId""")
@Serializable
class ClazzAssignmentContentJoin {

    @PrimaryKey(autoGenerate = true)
    var clazzAssignmentContentJoinUid: Long = 0

    var clazzAssignmentContentJoinContentUid : Long = 0

    var clazzAssignmentContentJoinAssignmentUid : Long = 0

    var clazzAssignmentContentJoinActive : Boolean = true

    @MasterChangeSeqNum
    var clazzAssignmentContentJoinMCSN: Long = 0

    @LocalChangeSeqNum
    var clazzAssignmentContentJoinLCSN: Long = 0

    @LastChangedBy
    var clazzAssignmentContentJoinLCB: Int = 0

    @LastChangedTime
    var clazzAssignmentContentJoinLct: Long = 0

    companion object {

        const val TABLE_ID = 521

    }

}