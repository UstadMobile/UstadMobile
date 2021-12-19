package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.ustadmobile.door.annotation.ReplicationDestinationNodeId
import com.ustadmobile.door.annotation.ReplicationEntityForeignKey
import com.ustadmobile.door.annotation.ReplicationTrackerProcessed
import com.ustadmobile.door.annotation.ReplicationVersionId

@Entity(primaryKeys = arrayOf("siteFk", "siteDestination"))
class SiteTrkr {

    @ReplicationEntityForeignKey
    var siteFk: Long = 0

    @ReplicationVersionId
    var siteVersionId: Long = 0

    @ReplicationDestinationNodeId
    var siteDestination: Long = 0

    @ReplicationTrackerProcessed
    @ColumnInfo(defaultValue = "0")
    var siteTrkrProcessed: Boolean = false

}