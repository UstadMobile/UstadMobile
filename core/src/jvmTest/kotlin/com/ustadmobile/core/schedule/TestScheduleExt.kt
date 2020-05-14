package com.ustadmobile.core.schedule

import com.ustadmobile.lib.db.entities.Schedule
import org.junit.Assert
import org.junit.Test

class TestScheduleExt  {

    @Test
    fun givenSchedule_whenNextOccurenceCalled_thenShouldMatch() {
        val timezoneOffset = 4 * 60 * 60 * 1000 //+4 hours
        val schedule = Schedule().apply {
            scheduleDay = Schedule.DAY_FRIDAY
            sceduleStartTime = 10 * 60 * 60 * 1000 //10am
            scheduleEndTime = 12 * 60 * 60 * 1000
        }

        val fromTime = 1589393140000 // Wed 13/May/2020

        val nextOccurence = schedule.nextOccurence(timezoneOffset, fromTime)
        val nextFromTime = nextOccurence.from.format("EEE, dd MMM yyyy HH:mm:ss z")
        val nextToTime = nextOccurence.to.format("EEE, dd MMM yyyy HH:mm:ss z")
        Assert.assertEquals("Next occurence start time = Friday 15/May 0600 UTC/1000 local",
                "Fri, 15 May 2020 06:00:00 UTC", nextFromTime)
        Assert.assertEquals("Next occurence finish time = Friday 15/May 0800 UTC/1000 local",
                "Fri, 15 May 2020 08:00:00 UTC", nextToTime)
    }

}