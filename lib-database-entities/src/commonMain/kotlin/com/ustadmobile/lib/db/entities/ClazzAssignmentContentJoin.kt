package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ClazzAssignmentContentJoin.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID,
        notifyOnUpdate =  [
        """
             SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, 
                  ${ClazzAssignment.TABLE_ID} AS tableId 
            FROM ChangeLog
                 JOIN ClazzAssignmentContentJoin 
                       ON ChangeLog.chTableId = $TABLE_ID 
                       AND ClazzAssignmentContentJoin.cacjUid = ChangeLog.chEntityPk
                 JOIN ClazzAssignment
                      ON ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid
                 JOIN Clazz 
                    ON Clazz.clazzUid = ClazzAssignment.caClazzUid 
               ${Clazz.JOIN_FROM_CLAZZ_TO_DEVICESESSION_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_ASSIGNMENT_SELECT}
                    ${Clazz.JOIN_FROM_CLAZZ_TO_DEVICESESSION_VIA_SCOPEDGRANT_PT2}  
            
        """
        ],
        syncFindAllQuery = """
             SELECT ClazzAssignmentContentJoin.* 
          FROM DeviceSession
               JOIN PersonGroupMember 
                    ON DeviceSession.dsPersonUid = PersonGroupMember.groupMemberPersonUid
               ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_ASSIGNMENT_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2} 
               JOIN ClazzAssignment
                    ON ClazzAssignment.caClazzUid = Clazz.clazzUid
               JOIN ClazzAssignmentContentJoin
                    ON ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid       
         WHERE DeviceSession.dsDeviceId = :clientId
            
        """)
@Serializable
class ClazzAssignmentContentJoin {

    @PrimaryKey(autoGenerate = true)
    var cacjUid: Long = 0

    var cacjContentUid : Long = 0

    var cacjAssignmentUid : Long = 0

    var cacjActive : Boolean = true

    @MasterChangeSeqNum
    var cacjMCSN: Long = 0

    @LocalChangeSeqNum
    var cacjLCSN: Long = 0

    @LastChangedBy
    var cacjLCB: Int = 0

    @LastChangedTime
    var cacjLct: Long = 0

    companion object {

        const val TABLE_ID = 521

    }

}