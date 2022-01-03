package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.ustadmobile.door.annotation.ReplicationDestinationNodeId
import com.ustadmobile.door.annotation.ReplicationEntityForeignKey
import com.ustadmobile.door.annotation.ReplicationPending
import com.ustadmobile.door.annotation.ReplicationVersionId
import kotlinx.serialization.Serializable

@Serializable
@Entity(primaryKeys = arrayOf("aeTrkrForeignKey", "aeTrkrDestination"))
data class AgentEntityTrkr(

    @ReplicationEntityForeignKey
    var aeTrkrForeignKey: Long = 0,

    @ReplicationVersionId
    var aeTrkrLastModified: Long  = 0,

    @ReplicationDestinationNodeId
    var aeTrkrDestination: Long = 0,

    @ColumnInfo(defaultValue = "0")
    @ReplicationPending
    var aeTrkrProcessed: Boolean = false


)