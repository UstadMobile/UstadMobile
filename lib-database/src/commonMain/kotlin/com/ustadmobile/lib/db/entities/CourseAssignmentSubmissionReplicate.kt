package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.annotation.ReplicationDestinationNodeId
import com.ustadmobile.door.annotation.ReplicationEntityForeignKey
import com.ustadmobile.door.annotation.ReplicationPending
import com.ustadmobile.door.annotation.ReplicationVersionId

@Entity(
        primaryKeys = arrayOf("casPk", "casDestination"),
        indices = arrayOf(Index(value = arrayOf("casPk", "casDestination", "casVersionId")),
                Index(value = arrayOf("casDestination", "casPending")))

)
class CourseAssignmentSubmissionReplicate {

    @ReplicationEntityForeignKey
    public var casPk: Long = 0

    @ColumnInfo(defaultValue = "0")
    @ReplicationVersionId
    public var casVersionId: Long = 0

    @ReplicationDestinationNodeId
    public var casDestination: Long = 0

    @ColumnInfo(defaultValue = "1")
    @ReplicationPending
    public var casPending: Boolean = true


}