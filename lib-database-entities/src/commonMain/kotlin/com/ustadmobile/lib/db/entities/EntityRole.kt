package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.ClientSyncManager
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable


@Entity(indices =[
    //Index to provide fields required in permission queries
    Index(value = ["erGroupUid", "erRoleUid", "erTableId"])
])
@SyncableEntity(tableId = EntityRole.TABLE_ID,
    notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${EntityRole.TABLE_ID} AS tableId FROM 
        ChangeLog
        JOIN EntityRole ON ChangeLog.chTableId = ${EntityRole.TABLE_ID} AND ChangeLog.chEntityPk = EntityRole.erUid
        JOIN PersonGroup ON PersonGroup.groupUid = EntityRole.erGroupUid
        JOIN PersonGroupMember ON PersonGroupMember.groupMemberGroupUid = PersonGroup.groupUid
        JOIN Person ON Person.personUid = PersonGroupMember.groupMemberPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid""",

        //An EntityRole change should result in a complete resync for those who the role is assigned to
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClientSyncManager.TABLEID_SYNC_ALL_TABLES} AS tableId FROM 
        ChangeLog
        JOIN EntityRole ON ChangeLog.chTableId = ${EntityRole.TABLE_ID} AND ChangeLog.chEntityPk = EntityRole.erUid
        JOIN PersonGroup ON PersonGroup.groupUid = EntityRole.erGroupUid
        JOIN PersonGroupMember ON PersonGroupMember.groupMemberGroupUid = PersonGroup.groupUid
        JOIN DeviceSession ON DeviceSession.dsPersonUid = PersonGroupMember.groupMemberPersonUid
        """

    ],
    syncFindAllQuery = """
        SELECT EntityRole.* FROM
        EntityRole
        JOIN PersonGroupMember ON PersonGroupMember.groupMemberGroupUid = EntityRole.erGroupUid
        JOIN Person ON Person.personUid = PersonGroupMember.groupMemberPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        WHERE DeviceSession.dsDeviceId = :clientId
        """)
@Serializable
open class EntityRole() {

    @PrimaryKey(autoGenerate = true)
    var erUid: Long = 0

    @MasterChangeSeqNum
    var erMasterCsn: Long = 0

    @LocalChangeSeqNum
    var erLocalCsn: Long = 0

    @LastChangedBy
    var erLastChangedBy: Int = 0

    @ColumnInfo(index = true)
    var erTableId: Int = 0

    @ColumnInfo(index = true)
    var erEntityUid: Long = 0

    @ColumnInfo(index = true)
    var erGroupUid: Long = 0

    @ColumnInfo(index = true)
    var erRoleUid: Long = 0

    var erActive: Boolean = false

    constructor(erTableId: Int, erEntityUid: Long, erGroupUid: Long, erRoleUid: Long) : this() {
        this.erTableId = erTableId
        this.erEntityUid = erEntityUid
        this.erGroupUid = erGroupUid
        this.erRoleUid = erRoleUid
        this.erActive = true
    }

    companion object {
        const val TABLE_ID = 47
    }

}
