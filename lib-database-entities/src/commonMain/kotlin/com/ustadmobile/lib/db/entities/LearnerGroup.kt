package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
//@SyncableEntity(tableId = LearnerGroup.TABLE_ID,
//    notifyOnUpdate = ["""
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${LearnerGroup.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN LearnerGroup
//                    ON ChangeLog.chTableId = ${LearnerGroup.TABLE_ID}
//                        AND ChangeLog.chEntityPk = LearnerGroup.learnerGroupUid
//               JOIN LearnerGroupMember
//                    ON LearnerGroupMember.learnerGroupMemberLgUid = LearnerGroup.learnerGroupUid
//               JOIN Person
//                    ON Person.personUid = LearnerGroupMember.learnerGroupMemberPersonUid
//                    ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
//                        ${Role.PERMISSION_PERSON_SELECT}
//                        ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
//        """],
//
//    syncFindAllQuery = """
//        SELECT LearnerGroup.*
//          FROM UserSession
//               JOIN PersonGroupMember
//                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
//               ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
//               JOIN LearnerGroupMember
//                    ON LearnerGroupMember.learnerGroupMemberPersonUid = Person.personUid
//               JOIN LearnerGroup
//                    ON LearnerGroup.learnerGroupUid = LearnerGroupMember.learnerGroupMemberLgUid
//              WHERE UserSession.usClientNodeId = :clientId
//                AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
//    """)
@ReplicateEntity(tableId = LearnerGroup.TABLE_ID, tracker = LearnerGroupTracker::class)
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

    @LastChangedTime
    @ReplicationVersionId
    var learnerGroupLct: Long = 0

    companion object {

        const val TABLE_ID = 301

    }
}