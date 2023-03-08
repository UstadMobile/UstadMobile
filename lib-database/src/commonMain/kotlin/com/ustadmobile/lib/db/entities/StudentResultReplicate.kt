package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.annotation.ReplicationDestinationNodeId
import com.ustadmobile.door.annotation.ReplicationEntityForeignKey
import com.ustadmobile.door.annotation.ReplicationPending
import com.ustadmobile.door.annotation.ReplicationVersionId

@Entity(
    primaryKeys = arrayOf("srPk", "srDestination"),
    indices = arrayOf(
        Index(value = arrayOf("srPk", "srDestination", "srVersionId")),
        Index(value = arrayOf("srDestination", "srPending"))
    )

)
data class StudentResultReplicate(
    @ReplicationEntityForeignKey
    var srPk: Long = 0,

    @ColumnInfo(defaultValue = "0")
    @ReplicationVersionId
    var srVersionId: Long = 0,

    @ReplicationDestinationNodeId
    var srDestination: Long = 0,

    @ColumnInfo(defaultValue = "1")
    @ReplicationPending
    var srPending: Boolean = true
)
