package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.annotation.SyncablePrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 17)
@Entity
class DateRange {


    @SyncablePrimaryKey
    @PrimaryKey(autoGenerate = true)
    var dateRangeUid: Long = 0

    @UmSyncLocalChangeSeqNum
    var dateRangeLocalChangeSeqNum: Long = 0

    @UmSyncMasterChangeSeqNum
    var dateRangeMasterChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var dateRangLastChangedBy: Int = 0

    var dateRangeFromDate: Long = 0

    var dateRangeToDate: Long = 0

    var dateRangeUMCalendarUid: Long = 0

    var isDateRangeActive: Boolean = false

    var dateRangeName: String? = null

    var dateRangeDesc: String? = null

    constructor(fromDate: Long, toDate: Long) {
        this.dateRangeFromDate = fromDate
        this.dateRangeToDate = toDate
        this.isDateRangeActive = true
    }

    constructor(fromDate: Long) {
        this.dateRangeFromDate = fromDate
        this.isDateRangeActive = true
    }

    constructor() {
        this.isDateRangeActive = true
    }
}
