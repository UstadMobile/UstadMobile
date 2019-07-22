package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.annotation.SyncablePrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 82)
@Entity
class DashboardEntryTag {

    @SyncablePrimaryKey
    @PrimaryKey(autoGenerate = true)
    var dashboardEntryTagUid: Long = 0

    var dashboardEntryTagDashboardEntryUid: Long = 0

    var dashboardEntryTagDashboardTagUid: Long = 0

    var dashboardEntryTagActive: Boolean = false

    @UmSyncMasterChangeSeqNum
    var dashboardEntryTagMCSN: Long = 0

    @UmSyncLocalChangeSeqNum
    var dashboardEntryTagLCSN: Long = 0

    @UmSyncLastChangedBy
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
