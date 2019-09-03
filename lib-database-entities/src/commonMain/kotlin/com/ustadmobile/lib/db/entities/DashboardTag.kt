package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity

@SyncableEntity(tableId = 81)
@Entity
class DashboardTag {

    @PrimaryKey(autoGenerate = true)
    var dashboardTagUid: Long = 0

    var dashboardTagTitle: String? = null

    var dashboardTagActive: Boolean = false

    @MasterChangeSeqNum
    var dashboardTagMCSN: Long = 0

    @LocalChangeSeqNum
    var dashboardTagLCSN: Long = 0

    @LastChangedBy
    var dashboardTagLCB: Int = 0

    constructor(title: String) {
        this.dashboardTagTitle = title
        this.dashboardTagActive = true
    }

    constructor() {

    }
}
