package com.ustadmobile.core.schedule

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.parse
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class TestClazzLogCreator {

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var httpClient : HttpClient

    private lateinit var okHttpClient: OkHttpClient

    val dateFormat = DateFormat("EEE, dd MMM yyyy HH:mm:ss z")

    @Before
    fun setup() {
        val nodeIdAndAuth = NodeIdAndAuth(Random.nextLong(), randomUuid().toString())
        db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
                "jdbc:sqlite:build/tmp/UmAppDatabase.sqlite", nodeId = 1L)
            .addSyncCallback(nodeIdAndAuth)
            .build()
            .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)

        okHttpClient = OkHttpClient()
        httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json()
            }
            install(HttpTimeout)

            engine {
                preconfigured = okHttpClient
            }
        }

        repo = db.asRepository(repositoryConfig(Any(), "http://localhost/dummy",
            nodeIdAndAuth.nodeId, nodeIdAndAuth.auth, httpClient, okHttpClient))
    }

    @After
    fun tearDown() {
        httpClient.close()
        try {
            repo.close()
        }catch (e: Exception) {
            //do nothing
        }
        try {
            db.close()
        }catch (e: Exception) {
            //do nothing
        }
    }

    private fun createClazzAndSchedule(clazzName: String, holidayCalendarUid: Long = 0,
                                       timezone: String? = null,
                                       scheduleBlock: Schedule.() -> Unit): Pair<Clazz, Schedule> {
        val testClazz = Clazz(clazzName).apply {
            clazzHolidayUMCalendarUid = holidayCalendarUid
            clazzTimeZone = timezone
            clazzUid = repo.clazzDao().insert(this)
        }
        val testClazzSchedule = Schedule().apply(scheduleBlock).apply {
            scheduleClazzUid = testClazz.clazzUid
            scheduleUid = repo.scheduleDao().insert(this)
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

        runBlocking {
            repo.createClazzLogs(fromTime, toTime)

            val createdLogs = db.clazzLogDao().findByClazzUidWithinTimeRange(testClazz.clazzUid,
                    fromTime, toTime)
            assertEquals("Created one clazz log for clazz schedule", 1,
                    createdLogs.size)
            assertEquals("Log date is as expected", "Fri, 15 May 2020 06:00:00 UTC",
                    DateTime.fromUnix(createdLogs[0].logDate).format(dateFormat))
            assertEquals("Created log has correctly set schedule uid",
                    createdLogs[0].clazzLogScheduleUid, testClazzSchedule.scheduleUid)

        }
    }

    @Test
    fun givenClazzWithScheduleInRangeAndOverlappingHoliday_whenCreateClazzLogsCalled_thenShouldBeCreatedAsCancelledWithNote() {
        val holidayCalendar = HolidayCalendar("Test Holiday Calendar", 0).apply {
            umCalendarUid = repo.holidayCalendarDao().insert(this)
        }

        val longWeekendHoliday = Holiday().apply {
            holStartTime = dateFormat.parse("Fri, 15 May 2020 00:00:00 UTC").utc.unixMillisLong
            holEndTime = dateFormat.parse("Sun, 17 May 2020 23:59:59 UTC").utc.unixMillisLong
            this.holHolidayCalendarUid = holidayCalendar.umCalendarUid
            holUid = repo.holidayDao().insert(this)
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
        runBlocking {
            repo.createClazzLogs(fromTime, toTime)
            val createdLogs = repo.clazzLogDao().findByClazzUidWithinTimeRange(testClazz.clazzUid,
                    fromTime, toTime)

            Assert.assertEquals("Created one ClazzLog", 1, createdLogs.size)
            val createdLog = createdLogs.first()

            Assert.assertEquals("Log created during holiday time is cancelled", true,
                    createdLog.clazzLogCancelled)
        }
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
        runBlocking {
            repo.createClazzLogs(fromTime, toTime)
            val numLogsCreatedBefore = db.clazzLogDao().findByClazzUidWithinTimeRange(testClazz.clazzUid,
                    fromTime, toTime).size

            //Call it again
            repo.createClazzLogs(fromTime, toTime)
            val allLogsCreated = db.clazzLogDao().findByClazzUidWithinTimeRange(testClazz.clazzUid,
                    fromTime, toTime)

            Assert.assertEquals("One log was created before the second call to createClazzLogs",
                    1, numLogsCreatedBefore)
            Assert.assertEquals("Only one log is created for schedule", 1,
                    allLogsCreated.size)
        }
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
        runBlocking {
            repo.createClazzLogs(fromTime, toTime, matchLocalFromDay = true)

            val createdLogs = db.clazzLogDao().findByClazzUidWithinTimeRange(testClazz.clazzUid,
                    fromTime, toTime)
            Assert.assertEquals("Created one clazz log for clazz schedule", 1,
                    createdLogs.size)
            Assert.assertEquals("Log date is as expected", "Fri, 15 May 2020 06:00:00 UTC",
                    DateTime.fromUnix(createdLogs[0].logDate).format(dateFormat))

        }
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
        runBlocking {
            repo.createClazzLogs(fromTime, toTime, matchLocalFromDay = true)

            val clazzLogsCreated = db.clazzLogDao().findByClazzUidWithinTimeRange(testClazz.clazzUid,
                    fromTime, toTime)

            assertEquals("No clazz logs were created because it was not yet the same day " +
                    "as per the local time for the class",
                    0, clazzLogsCreated.size)
        }
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
        runBlocking {
            repo.createClazzLogs(fromTime, toTime, matchLocalFromDay = true)

            testClazzSchedule.apply {
                sceduleStartTime = 12 * 60 * 60 * 1000 //12pm
                scheduleEndTime = 13 * 60 * 60 * 1000
            }
            repo.scheduleDao().update(testClazzSchedule)

            repo.createClazzLogs(fromTime, toTime, matchLocalFromDay = true)

            val clazzLogsCreated = db.clazzLogDao().findByClazzUidWithinTimeRange(testClazz.clazzUid,
                    fromTime, toTime).partition { it.clazzLogStatusFlag != ClazzLog.STATUS_RESCHEDULED }

            assertEquals("One clazz log is active", 1,
                    clazzLogsCreated.first.size)

            assertEquals("The new log time is updated to match the new schedule",
                    "Fri, 15 May 2020 08:00:00 UTC",
                    DateTime.fromUnix(clazzLogsCreated.first[0].logDate).format(dateFormat))

            assertEquals("One clazz log is marked as rescheduled", 1, clazzLogsCreated.second.size)
        }
    }


}