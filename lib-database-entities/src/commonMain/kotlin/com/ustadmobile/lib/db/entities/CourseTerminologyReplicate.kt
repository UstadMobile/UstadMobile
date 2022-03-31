package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.annotation.ReplicationDestinationNodeId
import com.ustadmobile.door.annotation.ReplicationEntityForeignKey
import com.ustadmobile.door.annotation.ReplicationPending
import com.ustadmobile.door.annotation.ReplicationVersionId

@Entity(
    primaryKeys = arrayOf("ctPk", "ctDestination"),
    indices = arrayOf(Index(value = arrayOf("ctPk", "ctDestination", "ctVersionId")),
        Index(value = arrayOf("ctDestination", "ctPending")))

)
public class CourseTerminologyReplicate {

    @ReplicationEntityForeignKey
    public var ctPk: Long = 0

    @ColumnInfo(defaultValue = "0")
    @ReplicationVersionId
    public var ctVersionId: Long = 0

    @ReplicationDestinationNodeId
    public var ctDestination: Long = 0

    @ColumnInfo(defaultValue = "1")
    @ReplicationPending
    public var ctPending: Boolean = true
}
