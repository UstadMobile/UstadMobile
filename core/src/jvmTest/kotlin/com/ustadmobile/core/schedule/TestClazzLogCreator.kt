package com.ustadmobile.core.schedule

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.parse
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.util.test.checkJndiSetup
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TestClazzLogCreator {

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    @Before
    fun setup() {
        checkJndiSetup()
        db = UmAppDatabase.Companion.getInstance(Any())
        db.clearAllTables()
    }

    @Test
    fun givenClazzWithScheduleInRange_whenCreateClazzLogsCalled_thenShouldCreateClazzLog() {
        val testClazz = Clazz("Test Clazz").apply {
            clazzUid = db.clazzDao.insert(this)
        }

        val testClazzSchedule = Schedule().apply {
            scheduleDay = Schedule.DAY_FRIDAY
            sceduleStartTime = 10 * 60 * 60 * 1000 //10am
            scheduleEndTime = 12 * 60 * 60 * 1000
            scheduleClazzUid = testClazz.clazzUid
            scheduleActive = true
            scheduleUid = db.scheduleDao.insert(this)
        }

        val dateFormat = DateFormat("EEE, dd MMM yyyy HH:mm:ss z")
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




}