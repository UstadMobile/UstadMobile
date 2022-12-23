package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(tableId = Schedule.TABLE_ID, tracker = ScheduleReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "schedule_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO Schedule(scheduleUid, sceduleStartTime, scheduleEndTime, scheduleDay, scheduleMonth, scheduleFrequency, umCalendarUid, scheduleClazzUid, scheduleMasterChangeSeqNum, scheduleLocalChangeSeqNum, scheduleLastChangedBy, scheduleLastChangedTime, scheduleActive) 
         VALUES (NEW.scheduleUid, NEW.sceduleStartTime, NEW.scheduleEndTime, NEW.scheduleDay, NEW.scheduleMonth, NEW.scheduleFrequency, NEW.umCalendarUid, NEW.scheduleClazzUid, NEW.scheduleMasterChangeSeqNum, NEW.scheduleLocalChangeSeqNum, NEW.scheduleLastChangedBy, NEW.scheduleLastChangedTime, NEW.scheduleActive) 
         /*psql ON CONFLICT (scheduleUid) DO UPDATE 
         SET sceduleStartTime = EXCLUDED.sceduleStartTime, scheduleEndTime = EXCLUDED.scheduleEndTime, scheduleDay = EXCLUDED.scheduleDay, scheduleMonth = EXCLUDED.scheduleMonth, scheduleFrequency = EXCLUDED.scheduleFrequency, umCalendarUid = EXCLUDED.umCalendarUid, scheduleClazzUid = EXCLUDED.scheduleClazzUid, scheduleMasterChangeSeqNum = EXCLUDED.scheduleMasterChangeSeqNum, scheduleLocalChangeSeqNum = EXCLUDED.scheduleLocalChangeSeqNum, scheduleLastChangedBy = EXCLUDED.scheduleLastChangedBy, scheduleLastChangedTime = EXCLUDED.scheduleLastChangedTime, scheduleActive = EXCLUDED.scheduleActive
         */"""
     ]
 )
))
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

    @LastChangedTime
    @ReplicationVersionId
    var scheduleLastChangedTime: Long = 0

    //active or removed
    var scheduleActive: Boolean = true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Schedule

        if (scheduleUid != other.scheduleUid) return false
        if (sceduleStartTime != other.sceduleStartTime) return false
        if (scheduleEndTime != other.scheduleEndTime) return false
        if (scheduleDay != other.scheduleDay) return false
        if (scheduleMonth != other.scheduleMonth) return false
        if (scheduleFrequency != other.scheduleFrequency) return false
        if (umCalendarUid != other.umCalendarUid) return false
        if (scheduleClazzUid != other.scheduleClazzUid) return false
        if (scheduleMasterChangeSeqNum != other.scheduleMasterChangeSeqNum) return false
        if (scheduleLocalChangeSeqNum != other.scheduleLocalChangeSeqNum) return false
        if (scheduleLastChangedBy != other.scheduleLastChangedBy) return false
        if (scheduleActive != other.scheduleActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = scheduleUid.hashCode()
        result = 31 * result + sceduleStartTime.hashCode()
        result = 31 * result + scheduleEndTime.hashCode()
        result = 31 * result + scheduleDay
        result = 31 * result + scheduleMonth
        result = 31 * result + scheduleFrequency
        result = 31 * result + umCalendarUid.hashCode()
        result = 31 * result + scheduleClazzUid.hashCode()
        result = 31 * result + scheduleMasterChangeSeqNum.hashCode()
        result = 31 * result + scheduleLocalChangeSeqNum.hashCode()
        result = 31 * result + scheduleLastChangedBy
        result = 31 * result + scheduleActive.hashCode()
        return result
    }

    companion object {

        const val TABLE_ID = 21

        val SCHEDULE_FREQUENCY_DAILY = 1
        val SCHEDULE_FREQUENCY_WEEKLY = 2

        val SCHEDULE_FREQUENCY_ONCE = 3
        val SCHEDULE_FREQUENCY_MONTHLY = 4
        val SCHEDULE_FREQUENCY_YEARLY = 5

        // Constants as per Klock
        val DAY_SUNDAY = 0
        val DAY_MONDAY = 1
        val DAY_TUESDAY = 2
        val DAY_WEDNESDAY = 3
        val DAY_THURSDAY = 4
        val DAY_FRIDAY = 5
        val DAY_SATURDAY = 6


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
