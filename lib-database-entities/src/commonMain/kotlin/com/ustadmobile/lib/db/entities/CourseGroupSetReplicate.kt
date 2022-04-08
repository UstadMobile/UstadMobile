package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.annotation.ReplicationDestinationNodeId
import com.ustadmobile.door.annotation.ReplicationEntityForeignKey
import com.ustadmobile.door.annotation.ReplicationPending
import com.ustadmobile.door.annotation.ReplicationVersionId

@Entity(
    primaryKeys = arrayOf("cgsPk", "cgsDestination"),
    indices = arrayOf(Index(value = arrayOf("cgsPk", "cgsDestination", "cgsVersionId")),
        Index(value = arrayOf("cgsDestination", "cgsPending")))

)
class CourseGroupSetReplicate {

    @ReplicationEntityForeignKey
    public var cgsPk: Long = 0

    @ColumnInfo(defaultValue = "0")
    @ReplicationVersionId
    public var cgsVersionId: Long = 0

    @ReplicationDestinationNodeId
    public var cgsDestination: Long = 0

    @ColumnInfo(defaultValue = "1")
    @ReplicationPending
    public var cgsPending: Boolean = true


}