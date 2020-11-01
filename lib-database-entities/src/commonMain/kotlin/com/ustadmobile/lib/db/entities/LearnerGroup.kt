package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = LearnerGroup.TABLE_ID,
    notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${LearnerGroup.TABLE_ID} AS tableId FROM 
        ChangeLog
        JOIN LearnerGroup ON ChangeLog.chTableId = ${LearnerGroup.TABLE_ID} AND ChangeLog.chEntityPk = LearnerGroup.learnerGroupUid
        JOIN LearnerGroupMember ON LearnerGroupMember.learnerGroupMemberLgUid = LearnerGroup.learnerGroupUid
        JOIN Person ON Person.personUid = LearnerGroupMember.learnerGroupMemberPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid"""],
    syncFindAllQuery = """
        SELECT LearnerGroup.* FROM 
        LearnerGroup
        JOIN LearnerGroupMember ON LearnerGroupMember.learnerGroupMemberLgUid = LearnerGroup.learnerGroupUid
        JOIN Person ON Person.personUid = LearnerGroupMember.learnerGroupMemberPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        WHERE DeviceSession.dsDeviceId = :clientId
    """)
@Serializable
class LearnerGroup {

    @PrimaryKey(autoGenerate = true)
    var learnerGroupUid: Long = 0

    var learnerGroupName: String? = null

    var learnerGroupDescription: String? = null

    var learnerGroupActive: Boolean = true

    @MasterChangeSeqNum
    var learnerGroupMCSN: Long = 0

    @LocalChangeSeqNum
    var learnerGroupCSN: Long = 0

    @LastChangedBy
    var learnerGroupLCB: Int = 0

    companion object {

        const val TABLE_ID = 301

    }
}