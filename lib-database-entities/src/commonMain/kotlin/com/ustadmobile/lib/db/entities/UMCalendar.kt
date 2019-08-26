package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

/**
 * Represents a Caledar which will be liked to multiple holidays, schedules etc
 * Its basically a collection of dates and time. (holidays and schedules)
 */
@UmEntity(tableId = 28)
@Entity
open class UMCalendar {

    @PrimaryKey(autoGenerate = true)
    var umCalendarUid: Long = 0

    //The name of this calendar
    var umCalendarName: String? = null

    //Category
    var umCalendarCategory: Int = 0

    //active
    var umCalendarActive: Boolean = false

    //Tester method- Please remove me later
    var isUmCalendarFlag: Boolean = false

    @UmSyncMasterChangeSeqNum
    var umCalendarMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var umCalendarLocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var umCalendarLastChangedBy: Int = 0

    constructor(name: String, category: Int) {
        this.umCalendarName = name
        this.umCalendarCategory = category
        this.umCalendarActive = true
    }

    constructor() {

    }

    companion object {

        const val CATEGORY_HOLIDAY = 1
    }
}
