package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

/**
 * Represents a Caledar which will be liked to multiple holidays, schedules etc
 * Its basically a collection of dates and time. (holidays and schedules)
 */
//@SyncableEntity(tableId = HolidayCalendar.TABLE_ID,
//        notifyOnUpdate = ["""
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${HolidayCalendar.TABLE_ID} AS tableId
//          FROM UserSession
//    """])
@Entity
@Serializable
open class HolidayCalendar() {

    @PrimaryKey(autoGenerate = true)
    var umCalendarUid: Long = 0

    //The name of this calendar
    var umCalendarName: String? = null

    //Category
    var umCalendarCategory: Int = CATEGORY_HOLIDAY

    //active
    var umCalendarActive: Boolean = true
    
    @MasterChangeSeqNum
    var umCalendarMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var umCalendarLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var umCalendarLastChangedBy: Int = 0

    @LastChangedTime
    var umCalendarLct: Long = 0

    constructor(name: String, category: Int): this() {
        this.umCalendarName = name
        this.umCalendarCategory = category
        this.umCalendarActive = true
    }

    companion object {

        const val TABLE_ID = 28

        const val CATEGORY_HOLIDAY = 1
    }
}
