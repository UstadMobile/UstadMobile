package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = LearnerGroupMember.TABLE_ID,
    notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${LearnerGroupMember.TABLE_ID} AS tableId FROM 
        ChangeLog
        JOIN LearnerGroupMember ON ChangeLog.chTableId = ${LearnerGroupMember.TABLE_ID} AND ChangeLog.chEntityPk = LearnerGroupMember.learnerGroupMemberUid
        JOIN Person ON Person.personUid = LearnerGroupMember.learnerGroupMemberPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid"""],
    syncFindAllQuery = """
        SELECT LearnerGroupMember.* FROM 
        LearnerGroupMember
        JOIN Person ON Person.personUid = LearnerGroupMember.learnerGroupMemberPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        WHERE DeviceSession.dsDeviceId = :clientId
    """)
@Serializable
open class LearnerGroupMember {

    @PrimaryKey(autoGenerate = true)
    var learnerGroupMemberUid: Long = 0

    var learnerGroupMemberPersonUid: Long = 0

    var learnerGroupMemberLgUid: Long = 0

    var learnerGroupMemberRole: Int = PARTICIPANT_ROLE

    var learnerGroupMemberActive: Boolean = true

    @MasterChangeSeqNum
    var learnerGroupMemberMCSN: Long = 0

    @LocalChangeSeqNum
    var learnerGroupMemberCSN: Long = 0

    @LastChangedBy
    var learnerGroupMemberLCB: Int = 0

    companion object {

        const val TABLE_ID = 300

        const val PRIMARY_ROLE = 1

        const val PARTICIPANT_ROLE = 2

    }

}