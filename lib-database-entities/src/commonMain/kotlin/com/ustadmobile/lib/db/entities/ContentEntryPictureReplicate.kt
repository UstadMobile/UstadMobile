
package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.annotation.ReplicationDestinationNodeId
import com.ustadmobile.door.annotation.ReplicationEntityForeignKey
import com.ustadmobile.door.annotation.ReplicationPending
import com.ustadmobile.door.annotation.ReplicationVersionId
import kotlinx.serialization.Serializable

@Entity(
    primaryKeys = arrayOf("cepPk", "cepDestination"),
    indices = arrayOf(Index(value = arrayOf("cepPk", "cepDestination", "cepVersionId")),
        Index(value = arrayOf("cepDestination", "cepPending")))

)
@Serializable
public class ContentEntryPictureReplicate {
    @ReplicationEntityForeignKey
    public var cepPk: Long = 0

    @ColumnInfo(defaultValue = "0")
    @ReplicationVersionId
    public var cepVersionId: Long = 0

    @ReplicationDestinationNodeId
    public var cepDestination: Long = 0

    @ColumnInfo(defaultValue = "1")
    @ReplicationPending
    public var cepPending: Boolean = true
}
