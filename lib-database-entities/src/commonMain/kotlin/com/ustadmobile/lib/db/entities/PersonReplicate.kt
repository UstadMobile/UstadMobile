// @Triggers(arrayOf(
//     Trigger(
//         name = "person_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO Person(personUid, username, firstNames, lastName, emailAddr, phoneNum, gender, active, admin, personNotes, fatherName, fatherNumber, motherName, motherNum, dateOfBirth, personAddress, personOrgId, personGroupUid, personMasterChangeSeqNum, personLocalChangeSeqNum, personLastChangedBy, personLct, personCountry, personType) 
//             VALUES (NEW.personUid, NEW.username, NEW.firstNames, NEW.lastName, NEW.emailAddr, NEW.phoneNum, NEW.gender, NEW.active, NEW.admin, NEW.personNotes, NEW.fatherName, NEW.fatherNumber, NEW.motherName, NEW.motherNum, NEW.dateOfBirth, NEW.personAddress, NEW.personOrgId, NEW.personGroupUid, NEW.personMasterChangeSeqNum, NEW.personLocalChangeSeqNum, NEW.personLastChangedBy, NEW.personLct, NEW.personCountry, NEW.personType) 
//             /*psql ON CONFLICT (personUid) DO UPDATE 
//             SET username = EXCLUDED.username, firstNames = EXCLUDED.firstNames, lastName = EXCLUDED.lastName, emailAddr = EXCLUDED.emailAddr, phoneNum = EXCLUDED.phoneNum, gender = EXCLUDED.gender, active = EXCLUDED.active, admin = EXCLUDED.admin, personNotes = EXCLUDED.personNotes, fatherName = EXCLUDED.fatherName, fatherNumber = EXCLUDED.fatherNumber, motherName = EXCLUDED.motherName, motherNum = EXCLUDED.motherNum, dateOfBirth = EXCLUDED.dateOfBirth, personAddress = EXCLUDED.personAddress, personOrgId = EXCLUDED.personOrgId, personGroupUid = EXCLUDED.personGroupUid, personMasterChangeSeqNum = EXCLUDED.personMasterChangeSeqNum, personLocalChangeSeqNum = EXCLUDED.personLocalChangeSeqNum, personLastChangedBy = EXCLUDED.personLastChangedBy, personLct = EXCLUDED.personLct, personCountry = EXCLUDED.personCountry, personType = EXCLUDED.personType
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO PersonReplicate(personPk, personVersionId, personDestination)
//      SELECT Person.personUid AS personUid,
//             Person.personLct AS personVersionId,
//             :newNodeId AS personDestination
//        FROM Person
//       WHERE Person.personLct != COALESCE(
//             (SELECT personVersionId
//                FROM PersonReplicate
//               WHERE personPk = Person.personUid
//                 AND personDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(personPk, personDestination) DO UPDATE
//             SET personPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([Person::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO PersonReplicate(personPk, personVersionId, personDestination)
//  SELECT Person.personUid AS personUid,
//         Person.personLct AS personVersionId,
//         UserSession.usClientNodeId AS personDestination
//    FROM ChangeLog
//         JOIN Person
//             ON ChangeLog.chTableId = 9
//                AND ChangeLog.chEntityPk = Person.personUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND Person.personLct != COALESCE(
//         (SELECT personVersionId
//            FROM PersonReplicate
//           WHERE personPk = Person.personUid
//             AND personDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(personPk, personDestination) DO UPDATE
//     SET personPending = true
//  */               
// """)
// @ReplicationRunOnChange([Person::class])
// @ReplicationCheckPendingNotificationsFor([Person::class])
// abstract suspend fun replicateOnChange()
package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.`annotation`.ReplicationDestinationNodeId
import com.ustadmobile.door.`annotation`.ReplicationEntityForeignKey
import com.ustadmobile.door.`annotation`.ReplicationPending
import com.ustadmobile.door.`annotation`.ReplicationVersionId
import kotlin.Boolean
import kotlin.Long
import kotlinx.serialization.Serializable

@Entity(
  primaryKeys = arrayOf("personPk", "personDestination"),
  indices = arrayOf(Index(value = arrayOf("personPk", "personDestination", "personVersionId")),
  Index(value = arrayOf("personDestination", "personPending")))

)
@Serializable
public class PersonReplicate {
  @ReplicationEntityForeignKey
  public var personPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var personVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var personDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var personPending: Boolean = true
}
