package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.annotation.ReplicationDestinationNodeId
import com.ustadmobile.door.annotation.ReplicationEntityForeignKey
import com.ustadmobile.door.annotation.ReplicationPending
import com.ustadmobile.door.annotation.ReplicationVersionId

@Entity(
        primaryKeys = arrayOf("cbPk", "cbDestination"),
        indices = arrayOf(Index(value = arrayOf("cbPk", "cbDestination", "cbVersionId")),
                Index(value = arrayOf("cbDestination", "cbPending")))

)
class CourseBlockReplicate {

    @ReplicationEntityForeignKey
    public var cbPk: Long = 0

    @ColumnInfo(defaultValue = "0")
    @ReplicationVersionId
    public var cbVersionId: Long = 0

    @ReplicationDestinationNodeId
    public var cbDestination: Long = 0

    @ColumnInfo(defaultValue = "1")
    @ReplicationPending
    public var cbPending: Boolean = true


}