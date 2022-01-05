package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = GroupLearningSession.TABLE_ID,
    tracker = GroupLearningSessionReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "grouplearningsession_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO GroupLearningSession(groupLearningSessionUid, groupLearningSessionContentUid, groupLearningSessionLearnerGroupUid, groupLearningSessionInactive, groupLearningSessionMCSN, groupLearningSessionCSN, groupLearningSessionLCB, groupLearningSessionLct) 
         VALUES (NEW.groupLearningSessionUid, NEW.groupLearningSessionContentUid, NEW.groupLearningSessionLearnerGroupUid, NEW.groupLearningSessionInactive, NEW.groupLearningSessionMCSN, NEW.groupLearningSessionCSN, NEW.groupLearningSessionLCB, NEW.groupLearningSessionLct) 
         /*psql ON CONFLICT (groupLearningSessionUid) DO UPDATE 
         SET groupLearningSessionContentUid = EXCLUDED.groupLearningSessionContentUid, groupLearningSessionLearnerGroupUid = EXCLUDED.groupLearningSessionLearnerGroupUid, groupLearningSessionInactive = EXCLUDED.groupLearningSessionInactive, groupLearningSessionMCSN = EXCLUDED.groupLearningSessionMCSN, groupLearningSessionCSN = EXCLUDED.groupLearningSessionCSN, groupLearningSessionLCB = EXCLUDED.groupLearningSessionLCB, groupLearningSessionLct = EXCLUDED.groupLearningSessionLct
         */"""
     ]
 )
))
//@SyncableEntity(tableId = GroupLearningSession.TABLE_ID,
//    notifyOnUpdate = ["""
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${GroupLearningSession.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN GroupLearningSession
//                    ON ChangeLog.chTableId = ${GroupLearningSession.TABLE_ID}
//                        AND ChangeLog.chEntityPk = GroupLearningSession.groupLearningSessionUid
//               JOIN LearnerGroupMember
//                    ON LearnerGroupMember.learnerGroupMemberLgUid = GroupLearningSession.groupLearningSessionLearnerGroupUid
//               JOIN Person
//                    ON Person.personUid = LearnerGroupMember.learnerGroupMemberPersonUid
//                ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
//                        """],
//    syncFindAllQuery = """
//        SELECT GroupLearningSession.*
//          FROM UserSession
//               JOIN PersonGroupMember
//                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
//               $JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1
//                    ${Role.PERMISSION_PERSON_SELECT}
//                    $JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2
//               JOIN LearnerGroupMember
//                    ON LearnerGroupMember.learnerGroupMemberPersonUid = Person.personUid
//               JOIN GroupLearningSession
//                    ON GroupLearningSession.groupLearningSessionLearnerGroupUid = LearnerGroupMember.learnerGroupMemberLgUid
//         WHERE UserSession.usClientNodeId = :clientId
//           AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
//         """
//)
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

    @LastChangedTime
    @ReplicationVersionId
    var groupLearningSessionLct: Long = 0


    companion object {

        const val TABLE_ID = 302

    }
}