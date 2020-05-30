package com.ustadmobile.core.schedule

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.parse
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TestClazzLogCreator {

    private lateinit var db: UmAppDatabase

    val dateFormat = DateFormat("EEE, dd MMM yyyy HH:mm:ss z")

    @Before
    fun setup() {
        checkJndiSetup()
        db = UmAppDatabase.Companion.getInstance(Any())
        db.clearAllTables()
    }

    private fun createClazzAndSchedule(clazzName: String, holidayCalendarUid: Long = 0,
                                       timezone: String? = null,
                                       scheduleBlock: Schedule.() -> Unit): Pair<Clazz, Schedule> {
        val testClazz = Clazz(clazzName).apply {
            clazzHolidayUMCalendarUid = holidayCalendarUid
            clazzTimeZone = timezone
            clazzUid = db.clazzDao.insert(this)
        }
        val testClazzSchedule = Schedule().apply(scheduleBlock).apply {
            scheduleClazzUid = testClazz.clazzUid
            scheduleUid = db.scheduleDao.insert(this)
        }

        return Pair(testClazz, testClazzSchedule)
    }


    @Test
    fun givenClazzWithScheduleInRange_whenCreateClazzLogsCalled_thenShouldCreateClazzLog() {
        val (testClazz, testClazzSchedule) = createClazzAndSchedule("Test Clazz",
                timezone = "Asia/Dubai") {
            scheduleDay = Schedule.DAY_FRIDAY
            sceduleStartTime = 10 * 60 * 60 * 1000 //10am
            scheduleEndTime = 12 * 60 * 60 * 1000
            scheduleActive = true
        }

        val fromTime = dateFormat.parse("Thu, 14 May 2020 20:00:00 UTC").utc.unixMillisLong
        val toTime = fromTime + (1.days.millisecondsLong)

        db.createClazzLogs(fromTime, toTime)


        val createdLogs = db.clazzLogDao.findByClazzUidWithinTimeRange(testClazz.clazzUid,
            fromTime, toTime)
        Assert.assertEquals("Created one clazz log for clazz schedule", 1,
                createdLogs.size)
        Assert.assertEquals("Log date is as expected", "Fri, 15 May 2020 06:00:00 UTC",
                DateTime.fromUnix(createdLogs[0].logDate).format(dateFormat))
        Assert.assertEquals("Created log has correctly set schedule uid",
                createdLogs[0].clazzLogScheduleUid, testClazzSchedule.scheduleUid)
    }

    @Test
    fun givenClazzWithScheduleInRangeAndOverlappingHoliday_whenCreateClazzLogsCalled_thenShouldBeCreatedAsCancelledWithNote() {
        val holidayCalendar = HolidayCalendar("Test Holiday Calendar", 0).apply {
            umCalendarUid = db.holidayCalendarDao.insert(this)
        }

        val longWeekendHoliday = Holiday().apply {
            holStartTime = dateFormat.parse("Fri, 15 May 2020 00:00:00 UTC").utc.unixMillisLong
            holEndTime = dateFormat.parse("Sun, 17 May 2020 23:59:59 UTC").utc.unixMillisLong
            this.holHolidayCalendarUid = holidayCalendar.umCalendarUid
            holUid = db.holidayDao.insert(this)
        }

        val (testClazz, testClazzSchedule) = createClazzAndSchedule("Test Clazz",
                holidayCalendar.umCalendarUid) {
            scheduleDay = Schedule.DAY_FRIDAY
            sceduleStartTime = 10 * 60 * 60 * 1000 //10am
            scheduleEndTime = 12 * 60 * 60 * 1000
            scheduleActive = true
        }

        val fromTime = dateFormat.parse("Thu, 14 May 2020 20:00:00 UTC").utc.unixMillisLong
        val toTime = fromTime + (1.days.millisecondsLong)

        db.createClazzLogs(fromTime, toTime)
        val createdLogs = db.clazzLogDao.findByClazzUidWithinTimeRange(testClazz.clazzUid,
                fromTime, toTime)

        Assert.assertEquals("Created one ClazzLog", 1, createdLogs.size)
        val createdLog = createdLogs.first()

        Assert.assertEquals("Log created during holiday time is cancelled", true,
            createdLog.clazzLogCancelled)
    }

    @Test
    fun givenClazzWithScheduleWithLogAlreadyCreated_whenCreateClazzLogsCalled_thenShouldNotCreateAgain() {
        val (testClazz, testClazzSchedule) = createClazzAndSchedule("Test Clazz") {
            scheduleDay = Schedule.DAY_FRIDAY
            sceduleStartTime = 10 * 60 * 60 * 1000 //10am
            scheduleEndTime = 12 * 60 * 60 * 1000
            scheduleActive = true
        }

        val fromTime = dateFormat.parse("Thu, 14 May 2020 20:00:00 UTC").utc.unixMillisLong
        val toTime = fromTime + (1.days.millisecondsLong)

        db.createClazzLogs(fromTime, toTime)
        val numLogsCreatedBefore = db.clazzLogDao.findByClazzUidWithinTimeRange(testClazz.clazzUid,
                fromTime, toTime).size

        //Call it again
        db.createClazzLogs(fromTime, toTime)
        val allLogsCreated = db.clazzLogDao.findByClazzUidWithinTimeRange(testClazz.clazzUid,
                fromTime, toTime)

        Assert.assertEquals("One log was created before the second call to createClazzLogs",
                1, numLogsCreatedBefore)
        Assert.assertEquals("Only one log is created for schedule", 1,
                allLogsCreated.size)
    }


    /**
     * Server mode log creation - in this instance the server is creating logs at 20:00 14/May UTC
     * (e.g. 00:00 15/May GMT+4). Therefor this should match the local day and the class log
     * should be created
     */
    @Test
    fun givenClazzWithScheduleInRangeAndSameLocalDay_whenCreateClazzLogsCalled_thenShouldBeCreated() {
        val (testClazz, testClazzSchedule) = createClazzAndSchedule("Test Clazz",
                timezone = "Asia/Dubai") {
            scheduleDay = Schedule.DAY_FRIDAY
            sceduleStartTime = 10 * 60 * 60 * 1000 //10am
            scheduleEndTime = 12 * 60 * 60 * 1000
            scheduleActive = true
        }

        val fromTime = dateFormat.parse("Thu, 14 May 2020 20:00:00 UTC").utc.unixMillisLong
        val toTime = fromTime + (1.days.millisecondsLong)

        db.createClazzLogs(fromTime, toTime, matchLocalFromDay = true)

        val createdLogs = db.clazzLogDao.findByClazzUidWithinTimeRange(testClazz.clazzUid,
                fromTime, toTime)
        Assert.assertEquals("Created one clazz log for clazz schedule", 1,
                createdLogs.size)
        Assert.assertEquals("Log date is as expected", "Fri, 15 May 2020 06:00:00 UTC",
                DateTime.fromUnix(createdLogs[0].logDate).format(dateFormat))
    }

    @Test
    fun givenClazzWithScheduleInRangeAndNotSameLocalDay_whenCreateClazzLogsCalled_thenShouldNotBeCreated() {
        val (testClazz, testClazzSchedule) = createClazzAndSchedule("Test Clazz",
                timezone = "Asia/Dubai") {
            scheduleDay = Schedule.DAY_FRIDAY
            sceduleStartTime = 10 * 60 * 60 * 1000 //10am
            scheduleEndTime = 12 * 60 * 60 * 1000
            scheduleActive = true
        }

        val fromTime = dateFormat.parse("Thu, 14 May 2020 18:00:00 UTC").utc.unixMillisLong
        val toTime = fromTime + (1.days.millisecondsLong)

        db.createClazzLogs(fromTime, toTime, matchLocalFromDay = true)

        val clazzLogsCreated = db.clazzLogDao.findByClazzUidWithinTimeRange(testClazz.clazzUid,
                fromTime, toTime)

        Assert.assertEquals("No clazz logs were created because it was not yet the same day " +
                "as per the local time for the class",
            0, clazzLogsCreated.size)
    }

    @Test
    fun givenClazzScheduleUpdatedForSameDay_whenCreateClazzLogsCalled_thenExistingRecordShouldBeUpdated() {
        val (testClazz, testClazzSchedule) = createClazzAndSchedule("Test Clazz",
                timezone = "Asia/Dubai") {
            scheduleDay = Schedule.DAY_FRIDAY
            sceduleStartTime = 10 * 60 * 60 * 1000 //10am
            scheduleEndTime = 12 * 60 * 60 * 1000
            scheduleActive = true
        }

        val fromTime = dateFormat.parse("Thu, 14 May 2020 20:00:00 UTC").utc.unixMillisLong
        val toTime = fromTime + (1.days.millisecondsLong)

        db.createClazzLogs(fromTime, toTime, matchLocalFromDay = true)

        testClazzSchedule.apply {
            sceduleStartTime = 12 * 60 * 60 * 1000 //12pm
            scheduleEndTime = 13 * 60 * 60 * 1000
        }
        db.scheduleDao.update(testClazzSchedule)

        db.createClazzLogs(fromTime, toTime, matchLocalFromDay = true)

        val clazzLogsCreated = db.clazzLogDao.findByClazzUidWithinTimeRange(testClazz.clazzUid,
                fromTime, toTime).partition { it.clazzLogStatusFlag != ClazzLog.STATUS_RESCHEDULED }

        assertEquals("One clazz log is active", 1,
                clazzLogsCreated.first.size)

        assertEquals("The new log time is updated to match the new schedule",
                "Fri, 15 May 2020 08:00:00 UTC",
                DateTime.fromUnix(clazzLogsCreated.first[0].logDate).format(dateFormat))

        assertEquals("One clazz log is marked as rescheduled", 1, clazzLogsCreated.second.size)
    }


}