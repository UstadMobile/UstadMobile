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
    primaryKeys = arrayOf("prarPk", "prarDestination"),
    indices = arrayOf(Index(value = arrayOf("prarPk", "prarDestination", "prarVersionId")),
        Index(value = arrayOf("prarDestination", "prarPending")))

)
@Serializable
public class PeerReviewerAllocationReplicate {
    @ReplicationEntityForeignKey
    public var prarPk: Long = 0

    @ColumnInfo(defaultValue = "0")
    @ReplicationVersionId
    public var prarVersionId: Long = 0

    @ReplicationDestinationNodeId
    public var prarDestination: Long = 0

    @ColumnInfo(defaultValue = "1")
    @ReplicationPending
    public var prarPending: Boolean = true
}
