package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = 21)
@Entity
@Serializable
class Schedule {

    @PrimaryKey(autoGenerate = true)
    var scheduleUid: Long = 0

    //Start time
    /**
     * Get the time of day that this schedule is to begin. This should be in ms from the beginning of
     * the day. E.g. 14:30 = (14.5 * 60 * 60 * 1000) ms
     *
     * @return time of the day that class is to begin for this scheduled instance
     */
    /**
     * Set the time of day that this schedule is to begin. This should be in ms from the beginning of
     * the day. E.g. 14:30 = (14.5 * 60 * 60 * 1000) ms
     *
     * @param sceduleStartTime time of the day that class is to begin for this scheduled instance
     */
    var sceduleStartTime: Long = 0

    //End time
    /**
     *
     * @return
     */
    var scheduleEndTime: Long = 0

    //What day for this frequency
    var scheduleDay: Int = 0

    //What month for this frequency
    var scheduleMonth: Int = 0

    // Frequency - Once, Daily, Every Week, Every Month, Every Year
    var scheduleFrequency: Int = 0

    //The Calendar this will be set to.
    var umCalendarUid: Long = 0

    //What clazz is this Schedule for
    var scheduleClazzUid: Long = 0

    @MasterChangeSeqNum
    var scheduleMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var scheduleLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var scheduleLastChangedBy: Int = 0

    //active or removed
    var scheduleActive: Boolean = false

    companion object {

        val SCHEDULE_FREQUENCY_DAILY = 1
        val SCHEDULE_FREQUENCY_WEEKLY = 2

        val SCHEDULE_FREQUENCY_ONCE = 3
        val SCHEDULE_FREQUENCY_MONTHLY = 4
        val SCHEDULE_FREQUENCY_YEARLY = 5

        val DAY_SUNDAY = 1
        val DAY_MONDAY = 2
        val DAY_TUESDAY = 3
        val DAY_WEDNESDAY = 4
        val DAY_THURSDAY = 5
        val DAY_FRIDAY = 6
        val DAY_SATURDAY = 7


        val MONTH_JANUARY = 1
        val MONTH_FEBUARY = 2
        val MONTH_MARCH = 3
        val MONTH_APRIL = 4
        val MONTH_MAY = 5
        val MONTH_JUNE = 6
        val MONTH_JULY = 7
        val MONTH_AUGUST = 8
        val MONTH_SEPTEMBER = 9
        val MONTH_OCTOBER = 10
        val MONTH_NOVEMBER = 11
        val MONTH_DECEMBER = 12
    }
}
