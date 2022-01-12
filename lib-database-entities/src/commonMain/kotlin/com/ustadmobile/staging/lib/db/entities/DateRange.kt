package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable


@Entity
@Serializable
class DateRange() {

    @PrimaryKey(autoGenerate = true)
    var dateRangeUid: Long = 0

    @LocalChangeSeqNum
    var dateRangeLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var dateRangeMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var dateRangLastChangedBy: Int = 0

    @LastChangedTime
    var dateRangeLct: Long = 0

    var dateRangeFromDate: Long = 0

    var dateRangeToDate: Long = 0

    var dateRangeUMCalendarUid: Long = 0

    var dateRangeName: String? = null

    var dateRangeDesc: String? = null

    var dateRangeActive: Boolean = true

    constructor(fromDate: Long, toDate: Long): this() {
        this.dateRangeFromDate = fromDate
        this.dateRangeToDate = toDate
    }

    constructor(fromDate: Long): this() {
        this.dateRangeFromDate = fromDate
    }

}
