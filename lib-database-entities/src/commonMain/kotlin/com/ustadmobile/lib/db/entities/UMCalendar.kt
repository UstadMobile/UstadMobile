package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

/**
 * Represents a Caledar which will be liked to multiple holidays, schedules etc
 * Its basically a collection of dates and time. (holidays and schedules)
 */
@SyncableEntity(tableId = 28)
@Entity
@Serializable
open class UMCalendar() {

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

    @MasterChangeSeqNum
    var umCalendarMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var umCalendarLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var umCalendarLastChangedBy: Int = 0

    constructor(name: String, category: Int): this() {
        this.umCalendarName = name
        this.umCalendarCategory = category
        this.umCalendarActive = true
    }

    companion object {

        const val CATEGORY_HOLIDAY = 1
    }
}
