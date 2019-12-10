package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = 83)
@Entity
@Serializable
class DashboardEntryTag {

    @PrimaryKey(autoGenerate = true)
    var dashboardEntryTagUid: Long = 0

    var dashboardEntryTagDashboardEntryUid: Long = 0

    var dashboardEntryTagDashboardTagUid: Long = 0

    var dashboardEntryTagActive: Boolean = false

    @MasterChangeSeqNum
    var dashboardEntryTagMCSN: Long = 0

    @LocalChangeSeqNum
    var dashboardEntryTagLCSN: Long = 0

    @LastChangedBy
    var dashboardEntryTagLCB: Int = 0

    constructor() {
        dashboardEntryTagActive = true
    }

    constructor(entryUid: Long, tagUid: Long) {
        dashboardEntryTagActive = true
        this.dashboardEntryTagDashboardEntryUid = entryUid
        this.dashboardEntryTagDashboardTagUid = tagUid
    }
}
