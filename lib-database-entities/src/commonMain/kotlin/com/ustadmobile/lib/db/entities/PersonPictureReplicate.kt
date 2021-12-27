// @Triggers(arrayOf(
//     Trigger(name = "personpicture_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO PersonPicture(personPictureUid, personPicturePersonUid, personPictureMasterCsn, personPictureLocalCsn, personPictureLastChangedBy, personPictureLct, personPictureUri, personPictureMd5, fileSize, picTimestamp, mimeType, personPictureActive) VALUES (NEW.personPictureUid, NEW.personPicturePersonUid, NEW.personPictureMasterCsn, NEW.personPictureLocalCsn, NEW.personPictureLastChangedBy, NEW.personPictureLct, NEW.personPictureUri, NEW.personPictureMd5, NEW.fileSize, NEW.picTimestamp, NEW.mimeType, NEW.personPictureActive) " +
//             "/*psql ON CONFLICT (personPictureUid) DO UPDATE SET personPicturePersonUid = EXCLUDED.personPicturePersonUid, personPictureMasterCsn = EXCLUDED.personPictureMasterCsn, personPictureLocalCsn = EXCLUDED.personPictureLocalCsn, personPictureLastChangedBy = EXCLUDED.personPictureLastChangedBy, personPictureLct = EXCLUDED.personPictureLct, personPictureUri = EXCLUDED.personPictureUri, personPictureMd5 = EXCLUDED.personPictureMd5, fileSize = EXCLUDED.fileSize, picTimestamp = EXCLUDED.picTimestamp, mimeType = EXCLUDED.mimeType, personPictureActive = EXCLUDED.personPictureActive*/"
//             ])
//     )
// )            
package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.`annotation`.ReplicationDestinationNodeId
import com.ustadmobile.door.`annotation`.ReplicationEntityForeignKey
import com.ustadmobile.door.`annotation`.ReplicationTrackerProcessed
import com.ustadmobile.door.`annotation`.ReplicationVersionId
import kotlin.Boolean
import kotlin.Long
import kotlinx.serialization.Serializable

@Entity(
  primaryKeys = arrayOf("ppPk", "ppDestination"),
  indices = arrayOf(Index(value = arrayOf("ppDestination", "ppProcessed", "ppPk")))
)
@Serializable
public class PersonPictureReplicate {
  @ReplicationEntityForeignKey
  public var ppPk: Long = 0

  @ReplicationVersionId
  public var ppVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ppDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var ppProcessed: Boolean = false
}
