package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.annotation.ReplicationDestinationNodeId
import com.ustadmobile.door.annotation.ReplicationEntityForeignKey
import com.ustadmobile.door.annotation.ReplicationTrackerProcessed
import com.ustadmobile.door.annotation.ReplicationVersionId
import kotlinx.serialization.Serializable

@Serializable
@Entity(primaryKeys = arrayOf("sgForeignKey", "sgVersionId"),
    indices = arrayOf(Index(value = arrayOf("sgDestination", "sgProcessed", "sgForeignKey"))))
class ScopedGrantTrkr {

    @ReplicationEntityForeignKey
    var sgForeignKey: Long = 0

    @ReplicationVersionId
    var sgVersionId: Long = 0

    @ReplicationDestinationNodeId
    var sgDestination: Long = 0

    @ReplicationTrackerProcessed
    var sgProcessed: Boolean = false

}