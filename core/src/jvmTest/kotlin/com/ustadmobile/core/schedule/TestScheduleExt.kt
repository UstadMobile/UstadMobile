package com.ustadmobile.core.schedule

import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert
import org.junit.Test

class TestScheduleExt  {

    @Test
    fun givenSchedule_whenNextOccurenceCalled_thenShouldMatch() {
        val schedule = Schedule().apply {
            scheduleDay = Schedule.DAY_FRIDAY
            sceduleStartTime = 10 * 60 * 60 * 1000 //10am
            scheduleEndTime = 12 * 60 * 60 * 1000
        }

        val fromTime = 1589393140000 // Wed 13/May/2020
        val fromInstant = Instant.fromEpochMilliseconds(fromTime)
        val timeZone = TimeZone.of("Asia/Dubai")


        val nextOccurence = schedule.nextOccurenceX(timeZone, fromInstant.toLocalDateTime(timeZone))

        val nextLocalDateTime = nextOccurence.first.toLocalDateTime(timeZone)

        //Next instance should be Friday 15/May/2020 at 10am Dubai time
        Assert.assertEquals(10, nextLocalDateTime.hour)
        Assert.assertEquals(15, nextLocalDateTime.dayOfMonth)
        Assert.assertEquals(5, nextLocalDateTime.monthNumber)
        Assert.assertEquals(2020, nextLocalDateTime.year)

        val finishLocalDateTime = nextOccurence.second.toLocalDateTime(timeZone)
        Assert.assertEquals(12, finishLocalDateTime.hour)
    }

}