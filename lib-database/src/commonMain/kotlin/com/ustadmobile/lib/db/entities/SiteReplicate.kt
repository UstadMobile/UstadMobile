// @Triggers(arrayOf(
//     Trigger(
//         name = "site_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO Site(siteUid, sitePcsn, siteLcsn, siteLcb, siteLct, siteName, guestLogin, registrationAllowed, authSalt) 
//             VALUES (NEW.siteUid, NEW.sitePcsn, NEW.siteLcsn, NEW.siteLcb, NEW.siteLct, NEW.siteName, NEW.guestLogin, NEW.registrationAllowed, NEW.authSalt) 
//             /*psql ON CONFLICT (siteUid) DO UPDATE 
//             SET sitePcsn = EXCLUDED.sitePcsn, siteLcsn = EXCLUDED.siteLcsn, siteLcb = EXCLUDED.siteLcb, siteLct = EXCLUDED.siteLct, siteName = EXCLUDED.siteName, guestLogin = EXCLUDED.guestLogin, registrationAllowed = EXCLUDED.registrationAllowed, authSalt = EXCLUDED.authSalt
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO SiteReplicate(sitePk, siteVersionId, siteDestination)
//      SELECT Site.siteUid AS siteUid,
//             Site.siteLct AS siteVersionId,
//             :newNodeId AS siteDestination
//        FROM Site
//       WHERE Site.siteLct != COALESCE(
//             (SELECT siteVersionId
//                FROM SiteReplicate
//               WHERE sitePk = Site.siteUid
//                 AND siteDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(sitePk, siteDestination) DO UPDATE
//             SET sitePending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([Site::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO SiteReplicate(sitePk, siteVersionId, siteDestination)
//  SELECT Site.siteUid AS siteUid,
//         Site.siteLct AS siteVersionId,
//         UserSession.usClientNodeId AS siteDestination
//    FROM ChangeLog
//         JOIN Site
//             ON ChangeLog.chTableId = 189
//                AND ChangeLog.chEntityPk = Site.siteUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND Site.siteLct != COALESCE(
//         (SELECT siteVersionId
//            FROM SiteReplicate
//           WHERE sitePk = Site.siteUid
//             AND siteDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(sitePk, siteDestination) DO UPDATE
//     SET sitePending = true
//  */               
// """)
// @ReplicationRunOnChange([Site::class])
// @ReplicationCheckPendingNotificationsFor([Site::class])
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
  primaryKeys = arrayOf("sitePk", "siteDestination"),
  indices = arrayOf(Index(value = arrayOf("sitePk", "siteDestination", "siteVersionId")),
  Index(value = arrayOf("siteDestination", "sitePending")))

)
@Serializable
public class SiteReplicate {
  @ReplicationEntityForeignKey
  public var sitePk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var siteVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var siteDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var sitePending: Boolean = true
}
