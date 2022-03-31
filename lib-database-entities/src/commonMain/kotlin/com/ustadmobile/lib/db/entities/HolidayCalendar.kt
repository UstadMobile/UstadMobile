package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

/**
 * Represents a Caledar which will be liked to multiple holidays, schedules etc
 * Its basically a collection of dates and time. (holidays and schedules)
 */
@Entity
@Serializable
@ReplicateEntity(tableId = HolidayCalendar.TABLE_ID, tracker = HolidayCalendarReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "holidaycalendar_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO HolidayCalendar(umCalendarUid, umCalendarName, umCalendarCategory, umCalendarActive, umCalendarMasterChangeSeqNum, umCalendarLocalChangeSeqNum, umCalendarLastChangedBy, umCalendarLct) 
         VALUES (NEW.umCalendarUid, NEW.umCalendarName, NEW.umCalendarCategory, NEW.umCalendarActive, NEW.umCalendarMasterChangeSeqNum, NEW.umCalendarLocalChangeSeqNum, NEW.umCalendarLastChangedBy, NEW.umCalendarLct) 
         /*psql ON CONFLICT (umCalendarUid) DO UPDATE 
         SET umCalendarName = EXCLUDED.umCalendarName, umCalendarCategory = EXCLUDED.umCalendarCategory, umCalendarActive = EXCLUDED.umCalendarActive, umCalendarMasterChangeSeqNum = EXCLUDED.umCalendarMasterChangeSeqNum, umCalendarLocalChangeSeqNum = EXCLUDED.umCalendarLocalChangeSeqNum, umCalendarLastChangedBy = EXCLUDED.umCalendarLastChangedBy, umCalendarLct = EXCLUDED.umCalendarLct
         */"""
     ]
 )
))
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
    @ReplicationVersionId
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
