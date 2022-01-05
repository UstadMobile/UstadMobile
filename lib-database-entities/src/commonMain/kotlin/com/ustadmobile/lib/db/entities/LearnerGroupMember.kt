package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
//@SyncableEntity(tableId = LearnerGroupMember.TABLE_ID,
//    notifyOnUpdate = ["""
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${LearnerGroupMember.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN LearnerGroupMember
//                    ON ChangeLog.chTableId = ${LearnerGroupMember.TABLE_ID}
//                        AND ChangeLog.chEntityPk = LearnerGroupMember.learnerGroupMemberUid
//               JOIN Person
//                    ON Person.personUid = LearnerGroupMember.learnerGroupMemberPersonUid
//               ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
//       """],
//    syncFindAllQuery = """
//        SELECT LearnerGroupMember.*
//          FROM UserSession
//          JOIN PersonGroupMember
//               ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
//          ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
//                ${Role.PERMISSION_PERSON_SELECT}
//                ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
//          JOIN LearnerGroupMember
//               ON LearnerGroupMember.learnerGroupMemberPersonUid = Person.personUid
//         WHERE UserSession.usClientNodeId = :clientId
//           AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
//    """)
@ReplicateEntity(tableId = LearnerGroupMember.TABLE_ID, tracker = LearnerGroupMemberReplicate::class)
@Serializable
@Triggers(arrayOf(
 Trigger(
     name = "learnergroupmember_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO LearnerGroupMember(learnerGroupMemberUid, learnerGroupMemberPersonUid, learnerGroupMemberLgUid, learnerGroupMemberRole, learnerGroupMemberActive, learnerGroupMemberMCSN, learnerGroupMemberCSN, learnerGroupMemberLCB, learnerGroupMemberLct) 
         VALUES (NEW.learnerGroupMemberUid, NEW.learnerGroupMemberPersonUid, NEW.learnerGroupMemberLgUid, NEW.learnerGroupMemberRole, NEW.learnerGroupMemberActive, NEW.learnerGroupMemberMCSN, NEW.learnerGroupMemberCSN, NEW.learnerGroupMemberLCB, NEW.learnerGroupMemberLct) 
         /*psql ON CONFLICT (learnerGroupMemberUid) DO UPDATE 
         SET learnerGroupMemberPersonUid = EXCLUDED.learnerGroupMemberPersonUid, learnerGroupMemberLgUid = EXCLUDED.learnerGroupMemberLgUid, learnerGroupMemberRole = EXCLUDED.learnerGroupMemberRole, learnerGroupMemberActive = EXCLUDED.learnerGroupMemberActive, learnerGroupMemberMCSN = EXCLUDED.learnerGroupMemberMCSN, learnerGroupMemberCSN = EXCLUDED.learnerGroupMemberCSN, learnerGroupMemberLCB = EXCLUDED.learnerGroupMemberLCB, learnerGroupMemberLct = EXCLUDED.learnerGroupMemberLct
         */"""
     ]
 )
))
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

    @LastChangedTime
    @ReplicationVersionId
    var learnerGroupMemberLct: Long = 0

    companion object {

        const val TABLE_ID = 300

        const val PRIMARY_ROLE = 1

        const val PARTICIPANT_ROLE = 2

    }

}