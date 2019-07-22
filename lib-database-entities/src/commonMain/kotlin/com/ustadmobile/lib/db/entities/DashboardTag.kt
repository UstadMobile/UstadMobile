package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.annotation.SyncablePrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 81)
@Entity
class DashboardTag {

    @SyncablePrimaryKey
    @PrimaryKey(autoGenerate = true)
    var dashboardTagUid: Long = 0

    var dashboardTagTitle: String? = null

    var isDashboardTagActive: Boolean = false

    @UmSyncMasterChangeSeqNum
    var dashboardTagMCSN: Long = 0

    @UmSyncLocalChangeSeqNum
    var dashboardTagLCSN: Long = 0

    @UmSyncLastChangedBy
    var dashboardTagLCB: Int = 0

    constructor(title: String) {
        this.dashboardTagTitle = title
        this.isDashboardTagActive = true
    }

    constructor() {

    }
}
