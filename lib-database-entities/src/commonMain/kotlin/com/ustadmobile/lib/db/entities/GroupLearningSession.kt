package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = GroupLearningSession.TABLE_ID,
    notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${GroupLearningSession.TABLE_ID} AS tableId FROM 
        ChangeLog
        JOIN GroupLearningSession ON ChangeLog.chTableId = ${GroupLearningSession.TABLE_ID} AND ChangeLog.chEntityPk = GroupLearningSession.groupLearningSessionUid
        JOIN LearnerGroup ON LearnerGroup.learnerGroupUid = GroupLearningSession.groupLearningSessionLearnerGroupUid
        JOIN LearnerGroupMember ON LearnerGroupMember.learnerGroupMemberLgUid = LearnerGroup.learnerGroupUid
        JOIN Person ON Person.personUid = LearnerGroupMember.learnerGroupMemberPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid"""],
    syncFindAllQuery = """
        SELECT GroupLearningSession.* FROM 
        GroupLearningSession
        JOIN LearnerGroup ON LearnerGroup.learnerGroupUid = GroupLearningSession.groupLearningSessionLearnerGroupUid
        JOIN LearnerGroupMember ON LearnerGroupMember.learnerGroupMemberLgUid = LearnerGroup.learnerGroupUid
        JOIN Person ON Person.personUid = LearnerGroupMember.learnerGroupMemberPersonUid
        JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
            ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_PERSON_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        WHERE DeviceSession.dsDeviceId = :clientId
    """)
@Serializable
class GroupLearningSession {

    @PrimaryKey(autoGenerate = true)
    var groupLearningSessionUid: Long = 0

    var groupLearningSessionContentUid : Long = 0

    var groupLearningSessionLearnerGroupUid : Long = 0

    var groupLearningSessionInactive : Boolean = false

    @MasterChangeSeqNum
    var groupLearningSessionMCSN: Long = 0

    @LocalChangeSeqNum
    var groupLearningSessionCSN: Long = 0

    @LastChangedBy
    var groupLearningSessionLCB: Int = 0


    companion object {

        const val TABLE_ID = 302

    }
}