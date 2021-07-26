package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.ClientSyncManager
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = PersonGroupMember.TABLE_ID,
    notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, 
               ${PersonGroupMember.TABLE_ID} AS tableId 
          FROM ChangeLog
               JOIN PersonGroupMember
                    ON ChangeLog.chTableId = ${PersonGroupMember.TABLE_ID} 
                       AND ChangeLog.chEntityPk = PersonGroupMember.groupMemberUid
               JOIN Person
                    ON PersonGroupMember.groupMemberPersonUid = Person.personUid
               ${Person.JOIN_FROM_PERSON_TO_DEVICESESSION_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_PERSON_SELECT}
                    ${Person.JOIN_FROM_PERSON_TO_DEVICESESSION_VIA_SCOPEDGRANT_PT2}""",

        //anyone who has been added/removed from the group by this should do a complete resync
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, 
                ${ClientSyncManager.TABLEID_SYNC_ALL_TABLES} AS tableId 
           FROM ChangeLog
                JOIN PersonGroupMember 
                     ON ChangeLog.chTableId = ${PersonGroupMember.TABLE_ID} 
                        AND ChangeLog.chEntityPk = PersonGroupMember.groupMemberUid
                JOIN DeviceSession 
                     ON DeviceSession.dsPersonUid = PersonGroupMember.groupMemberPersonUid
        """
    ],
    syncFindAllQuery = """
        SELECT PersonGroupMember.* 
          FROM DeviceSession
               JOIN PersonGroupMember
                    ON DeviceSession.dsPersonUid = PersonGroupMember.groupMemberPersonUid
               ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_PERSON_SELECT}
                    ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
         WHERE DeviceSession.dsDeviceId = :clientId""")
@Serializable
class PersonGroupMember() {


    @PrimaryKey(autoGenerate = true)
    var groupMemberUid: Long = 0

    var groupMemberActive: Boolean = true

    @ColumnInfo(index = true)
    var groupMemberPersonUid: Long = 0

    @ColumnInfo(index = true)
    var groupMemberGroupUid: Long = 0

    @MasterChangeSeqNum
    var groupMemberMasterCsn: Long = 0

    @LocalChangeSeqNum
    var groupMemberLocalCsn: Long = 0

    @LastChangedBy
    var groupMemberLastChangedBy: Int = 0

    @LastChangedTime
    var groupMemberLct: Long = 0

    constructor(personUid:Long, groupUid:Long) : this(){
        this.groupMemberPersonUid = personUid
        this.groupMemberGroupUid = groupUid
    }

    companion object {
        const val TABLE_ID = 44
    }
}
