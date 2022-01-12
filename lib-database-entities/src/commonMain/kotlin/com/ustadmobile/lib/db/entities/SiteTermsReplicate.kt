// @Triggers(arrayOf(
//     Trigger(
//         name = "siteterms_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO SiteTerms(sTermsUid, termsHtml, sTermsLang, sTermsLangUid, sTermsActive, sTermsLastChangedBy, sTermsPrimaryCsn, sTermsLocalCsn, sTermsLct) 
//             VALUES (NEW.sTermsUid, NEW.termsHtml, NEW.sTermsLang, NEW.sTermsLangUid, NEW.sTermsActive, NEW.sTermsLastChangedBy, NEW.sTermsPrimaryCsn, NEW.sTermsLocalCsn, NEW.sTermsLct) 
//             /*psql ON CONFLICT (sTermsUid) DO UPDATE 
//             SET termsHtml = EXCLUDED.termsHtml, sTermsLang = EXCLUDED.sTermsLang, sTermsLangUid = EXCLUDED.sTermsLangUid, sTermsActive = EXCLUDED.sTermsActive, sTermsLastChangedBy = EXCLUDED.sTermsLastChangedBy, sTermsPrimaryCsn = EXCLUDED.sTermsPrimaryCsn, sTermsLocalCsn = EXCLUDED.sTermsLocalCsn, sTermsLct = EXCLUDED.sTermsLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO SiteTermsReplicate(stPk, stVersionId, stDestination)
//      SELECT SiteTerms.sTermsUid AS stUid,
//             SiteTerms.sTermsLct AS stVersionId,
//             :newNodeId AS stDestination
//        FROM SiteTerms
//       WHERE SiteTerms.sTermsLct != COALESCE(
//             (SELECT stVersionId
//                FROM SiteTermsReplicate
//               WHERE stPk = SiteTerms.sTermsUid
//                 AND stDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(stPk, stDestination) DO UPDATE
//             SET stPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([SiteTerms::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO SiteTermsReplicate(stPk, stVersionId, stDestination)
//  SELECT SiteTerms.sTermsUid AS stUid,
//         SiteTerms.sTermsLct AS stVersionId,
//         UserSession.usClientNodeId AS stDestination
//    FROM ChangeLog
//         JOIN SiteTerms
//             ON ChangeLog.chTableId = 272
//                AND ChangeLog.chEntityPk = SiteTerms.sTermsUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND SiteTerms.sTermsLct != COALESCE(
//         (SELECT stVersionId
//            FROM SiteTermsReplicate
//           WHERE stPk = SiteTerms.sTermsUid
//             AND stDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(stPk, stDestination) DO UPDATE
//     SET stPending = true
//  */               
// """)
// @ReplicationRunOnChange([SiteTerms::class])
// @ReplicationCheckPendingNotificationsFor([SiteTerms::class])
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
  primaryKeys = arrayOf("stPk", "stDestination"),
  indices = arrayOf(Index(value = arrayOf("stPk", "stDestination", "stVersionId")),
  Index(value = arrayOf("stDestination", "stPending")))

)
@Serializable
public class SiteTermsReplicate {
  @ReplicationEntityForeignKey
  public var stPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var stVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var stDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var stPending: Boolean = true
}
