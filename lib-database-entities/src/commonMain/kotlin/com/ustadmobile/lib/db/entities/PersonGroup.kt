package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = PersonGroup.TABLE_ID,
    notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${PersonGroup.TABLE_ID} AS tableId FROM 
        ChangeLog
        JOIN PersonGroup ON ChangeLog.chTableId = ${PersonGroup.TABLE_ID} AND ChangeLog.chEntityPk = PersonGroup.groupUid
        JOIN PersonGroupMember ON PersonGroupMember.groupMemberGroupUid = PersonGroup.groupUid
        JOIN Person ON Person.personUid = PersonGroupMember.groupMemberPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid"""],
    syncFindAllQuery = """
        SELECT PersonGroup.* FROM 
        PersonGroup
        JOIN PersonGroupMember ON PersonGroupMember.groupMemberGroupUid = PersonGroup.groupUid
        JOIN Person ON Person.personUid = PersonGroupMember.groupMemberPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        WHERE DeviceSession.dsDeviceId = :clientId
        """)
@Serializable
open class PersonGroup() {

    @PrimaryKey(autoGenerate = true)
    var groupUid: Long = 0

    @MasterChangeSeqNum
    var groupMasterCsn: Long = 0

    @LocalChangeSeqNum
    var groupLocalCsn: Long = 0

    @LastChangedBy
    var groupLastChangedBy: Int = 0

    var groupName: String? = null

    var groupActive : Boolean = true

    /**
     *
     */
    var personGroupFlag: Int = 0

    constructor(name: String) : this() {
        this.groupName = name
    }

    companion object{

        const val TABLE_ID = 43

        const val PERSONGROUP_FLAG_DEFAULT = 0
        const val PERSONGROUP_FLAG_PERSONGROUP = 1
    }
}
