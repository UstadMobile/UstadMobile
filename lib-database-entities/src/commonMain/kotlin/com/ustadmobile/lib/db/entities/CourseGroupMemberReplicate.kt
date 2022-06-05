package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.annotation.ReplicationDestinationNodeId
import com.ustadmobile.door.annotation.ReplicationEntityForeignKey
import com.ustadmobile.door.annotation.ReplicationPending
import com.ustadmobile.door.annotation.ReplicationVersionId

@Entity(
    primaryKeys = arrayOf("cgmPk", "cgmDestination"),
    indices = arrayOf(Index(value = arrayOf("cgmPk", "cgmDestination", "cgmVersionId")),
        Index(value = arrayOf("cgmDestination", "cgmPending")))

)
class CourseGroupMemberReplicate {

    @ReplicationEntityForeignKey
    public var cgmPk: Long = 0

    @ColumnInfo(defaultValue = "0")
    @ReplicationVersionId
    public var cgmVersionId: Long = 0

    @ReplicationDestinationNodeId
    public var cgmDestination: Long = 0

    @ColumnInfo(defaultValue = "1")
    @ReplicationPending
    public var cgmPending: Boolean = true


}