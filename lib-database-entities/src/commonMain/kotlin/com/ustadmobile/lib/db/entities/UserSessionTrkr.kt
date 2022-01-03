package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.ustadmobile.door.annotation.ReplicationDestinationNodeId
import com.ustadmobile.door.annotation.ReplicationEntityForeignKey
import com.ustadmobile.door.annotation.ReplicationPending
import com.ustadmobile.door.annotation.ReplicationVersionId

@Entity(primaryKeys = arrayOf("usForeignKey", "usDestination"))
class UserSessionTrkr {

    @ReplicationEntityForeignKey
    var usForeignKey: Long = 0

    @ReplicationVersionId
    var usVersionId: Long = 0

    @ReplicationDestinationNodeId
    var usDestination: Long = 0

    @ColumnInfo(defaultValue = "0")
    @ReplicationPending
    var usTrkrProcessed: Boolean = false



}