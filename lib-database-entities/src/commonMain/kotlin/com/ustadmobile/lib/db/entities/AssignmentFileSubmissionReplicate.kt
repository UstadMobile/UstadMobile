package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.annotation.ReplicationDestinationNodeId
import com.ustadmobile.door.annotation.ReplicationEntityForeignKey
import com.ustadmobile.door.annotation.ReplicationPending
import com.ustadmobile.door.annotation.ReplicationVersionId

@Entity(
        primaryKeys = arrayOf("afsPk", "afsDestination"),
        indices = arrayOf(Index(value = arrayOf("afsPk", "afsVersionId", "afsVersionId")),
                Index(value = arrayOf("afsDestination", "afsPending")))

)
class AssignmentFileSubmissionReplicate {

    @ReplicationEntityForeignKey
    public var afsPk: Long = 0

    @ColumnInfo(defaultValue = "0")
    @ReplicationVersionId
    public var afsVersionId: Long = 0

    @ReplicationDestinationNodeId
    public var afsDestination: Long = 0

    @ColumnInfo(defaultValue = "1")
    @ReplicationPending
    public var afsPending: Boolean = true


}