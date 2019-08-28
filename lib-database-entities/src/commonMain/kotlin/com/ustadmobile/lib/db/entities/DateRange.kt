package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity


@SyncableEntity(tableId = 17)
@Entity
class DateRange(var dateRangeActive: Boolean = true) {

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

    var dateRangeName: String? = null

    var dateRangeDesc: String? = null

    constructor(fromDate: Long, toDate: Long): this(true) {
        this.dateRangeFromDate = fromDate
        this.dateRangeToDate = toDate
    }

    constructor(fromDate: Long): this(true) {
        this.dateRangeFromDate = fromDate
    }

}
