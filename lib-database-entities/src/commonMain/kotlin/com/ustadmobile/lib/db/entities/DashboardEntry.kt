package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity


@SyncableEntity(tableId = 80)
@Entity
class DashboardEntry {
    
    @PrimaryKey(autoGenerate = true)
    var dashboardEntryUid: Long = 0

    var dashboardEntryPersonUid: Long = 0

    var dashboardEntryTitle: String? = null

    var dashboardEntryReportParam: String? = null

    var dashboardEntryIndex: Int = 0

    var dashboardEntryReportType: Int = 0

    var dashboardEntryActive: Boolean = false

    @MasterChangeSeqNum
    var dashboardEntryMCSN: Long = 0

    @LocalChangeSeqNum
    var dashboardEntryLCSN: Long = 0

    @LastChangedBy
    var dashboardEntryLCB: Int = 0

    constructor() {
        this.dashboardEntryActive = false
    }

    constructor(title: String, reportType: Int, personUid: Long) {
        this.dashboardEntryTitle = title
        this.dashboardEntryReportType = reportType
        this.dashboardEntryPersonUid = personUid
        this.dashboardEntryActive = true
        this.dashboardEntryIndex = 42
    }

    companion object {

        val REPORT_CHART_TYPE_BAR_CHART = 1
        val REPORT_CHART_TYPE_TABLE = 2

        val REPORT_TYPE_SALES_PERFORMANCE = 3
        val REPORT_TYPE_SALES_LOG = 4
        val REPORT_TYPE_TOP_LES = 5
    }
}
