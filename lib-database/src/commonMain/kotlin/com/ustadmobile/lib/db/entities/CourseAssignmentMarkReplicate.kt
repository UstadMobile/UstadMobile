package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.annotation.ReplicationDestinationNodeId
import com.ustadmobile.door.annotation.ReplicationEntityForeignKey
import com.ustadmobile.door.annotation.ReplicationPending
import com.ustadmobile.door.annotation.ReplicationVersionId

@Entity(
        primaryKeys = arrayOf("camPk", "camDestination"),
        indices = arrayOf(Index(value = arrayOf("camPk", "camDestination", "camVersionId")),
                Index(value = arrayOf("camDestination", "camPending")))

)
class CourseAssignmentMarkReplicate {

    @ReplicationEntityForeignKey
    public var camPk: Long = 0

    @ColumnInfo(defaultValue = "0")
    @ReplicationVersionId
    public var camVersionId: Long = 0

    @ReplicationDestinationNodeId
    public var camDestination: Long = 0

    @ColumnInfo(defaultValue = "1")
    @ReplicationPending
    public var camPending: Boolean = true


}