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
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${PersonGroupMember.TABLE_ID} AS tableId FROM 
        ChangeLog
        JOIN PersonGroupMember ON ChangeLog.chTableId = ${PersonGroupMember.TABLE_ID} AND ChangeLog.chEntityPk = PersonGroupMember.groupMemberUid
        JOIN Person ON Person.personUid = PersonGroupMember.groupMemberPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid""",

        //anyone who has been added/removed from the group by this should do a complete resync
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClientSyncManager.TABLEID_SYNC_ALL_TABLES} AS tableId FROM 
        ChangeLog
        JOIN PersonGroupMember ON ChangeLog.chTableId = ${PersonGroupMember.TABLE_ID} AND ChangeLog.chEntityPk = PersonGroupMember.groupMemberUid
        JOIN DeviceSession ON DeviceSession.dsPersonUid = PersonGroupMember.groupMemberPersonUid
        """
    ],
    syncFindAllQuery = """
        SELECT PersonGroupMember.* FROM 
        PersonGroupMember
        JOIN Person ON Person.personUid = PersonGroupMember.groupMemberPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        WHERE DeviceSession.dsDeviceId = :clientId
        """)
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
