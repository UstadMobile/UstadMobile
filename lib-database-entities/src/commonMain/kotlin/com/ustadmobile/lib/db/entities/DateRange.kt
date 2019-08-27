package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity


@SyncableEntity(tableId = 17)
@Entity
class DateRange {

    @PrimaryKey(autoGenerate = true)
    var dateRangeUid: Long = 0

    @LocalChangeSeqNum
    var dateRangeLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var dateRangeMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var dateRangLastChangedBy: Int = 0

    var dateRangeFromDate: Long = 0

    var dateRangeToDate: Long = 0

    var dateRangeUMCalendarUid: Long = 0

    var dateRangeActive: Boolean = false

    var dateRangeName: String? = null

    var dateRangeDesc: String? = null

    constructor(fromDate: Long, toDate: Long) {
        this.dateRangeFromDate = fromDate
        this.dateRangeToDate = toDate
        this.dateRangeActive = true
    }

    constructor(fromDate: Long) {
        this.dateRangeFromDate = fromDate
        this.dateRangeActive = true
    }

    constructor() {
        this.dateRangeActive = true
    }
}
