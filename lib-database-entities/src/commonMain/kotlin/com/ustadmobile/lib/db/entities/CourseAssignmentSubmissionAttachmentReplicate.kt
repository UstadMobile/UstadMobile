package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.annotation.ReplicationDestinationNodeId
import com.ustadmobile.door.annotation.ReplicationEntityForeignKey
import com.ustadmobile.door.annotation.ReplicationPending
import com.ustadmobile.door.annotation.ReplicationVersionId

@Entity(
        primaryKeys = arrayOf("casaPk", "casaDestination"),
        indices = arrayOf(Index(value = arrayOf("casaPk", "casaDestination", "casaVersionId")),
                Index(value = arrayOf("casaDestination", "casaPending")))

)
class CourseAssignmentSubmissionAttachmentReplicate {

    @ReplicationEntityForeignKey
    public var casaPk: Long = 0

    @ColumnInfo(defaultValue = "0")
    @ReplicationVersionId
    public var casaVersionId: Long = 0

    @ReplicationDestinationNodeId
    public var casaDestination: Long = 0

    @ColumnInfo(defaultValue = "1")
    @ReplicationPending
    public var casaPending: Boolean = true


}