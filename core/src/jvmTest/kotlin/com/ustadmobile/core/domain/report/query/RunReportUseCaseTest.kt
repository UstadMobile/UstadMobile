package com.ustadmobile.core.domain.report.query

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.report.model.RelativeRangeReportPeriod
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportPeriodOption
import com.ustadmobile.core.domain.report.model.ReportTimeRangeUnit
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.SystemPermission
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunReportUseCaseTest {

    private lateinit var db: UmAppDatabase

    private lateinit var runReportUseCase: RunReportUseCase

    @BeforeTest
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite::memory:", 1L)
            .build()
        runReportUseCase = RunReportUseCaseDatabaseImpl(db, GenerateReportQueriesUseCase())
    }

    private val defaultNumStatementsPerDay = 2
    private val defaultDurationPerStatement = 2_000L
    private val defaultNumDays = 3

    private val defaultAccountPersonUid = 1L

    data class StatementsInsertedInfo(
        val statements: List<StatementEntity>,
    )

    private fun insertStatementsPerDay(
        numStatementsPerDay: Int = defaultNumStatementsPerDay,
        durationPerStatement: Long = defaultDurationPerStatement,
        numDays: Int = defaultNumDays,
    ) : StatementsInsertedInfo{
        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        val statementList = (0 until numDays).flatMap { dayIndex ->
            //Adding 24 hours does not always get to the same time next day e.g. when daylight
            // savings time changes. Use LocalDateTime to workaround this.
            val timestamp = LocalDateTime(
                today.date.minus(DatePeriod(days = dayIndex)), today.time
            ).toInstant(TimeZone.UTC)

            (1..numStatementsPerDay).map {
                val statementUid = uuid4()
                StatementEntity(
                    statementIdHi = statementUid.mostSignificantBits,
                    statementIdLo = statementUid.leastSignificantBits,
                    timestamp = timestamp.toEpochMilliseconds(),
                    resultDuration = durationPerStatement,
                )
            }
        }

        runBlocking {
            db.statementDao().insertOrIgnoreListAsync(statementList)
        }

        return StatementsInsertedInfo(statementList)
    }

    private fun grantLearningRecordViewSystemPermission(
        personUid: Long = defaultAccountPersonUid
    ) {
        runBlocking {
            db.systemPermissionDao().upsertAsync(
                SystemPermission(
                    spToPersonUid = personUid,
                    spPermissionsFlag = PermissionFlags.COURSE_LEARNINGRECORD_VIEW
                )
            )
        }
    }


    @Test
    fun givenStatementsInDatabase_whenDurationPerDayQueried_thenResultsAsExpected() {
        insertStatementsPerDay()
        grantLearningRecordViewSystemPermission()

        val results = runBlocking {
            runReportUseCase(
                request = RunReportUseCase.RunReportRequest(
                    reportOptions = ReportOptions2(
                        xAxis = ReportXAxis.DAY,
                        series = listOf(
                            ReportSeries2(
                                reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION
                            )
                        ),
                        period = ReportPeriodOption.LAST_WEEK.period,
                    ),
                    accountPersonUid = defaultAccountPersonUid,
                    timeZone = TimeZone.UTC,
                )
            )
        }.results.first()

        assertEquals(7, results.size,
            "result size equals number of days of reporting period - LAST_WEEK - 7 days")

        (0 until defaultNumDays).forEach { dayIndex ->
            val localDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
                .minus(DatePeriod(days = dayIndex))

            assertEquals(
                expected = (defaultDurationPerStatement * defaultNumStatementsPerDay).toDouble(),
                actual = results.find { it.xAxis == localDate.toString() }!!.yAxis,
                message = "day $dayIndex has expected total duration"
            )
        }
    }

    @Test
    fun givenStatementsInDatabase_whenDurationPerWeekQueried_thenResultsAsExpected() {
        val numWeeks = 3
        val numDaysStatements = numWeeks * 7
        insertStatementsPerDay(
            numDays = numDaysStatements,
        )
        grantLearningRecordViewSystemPermission()

        val request = RunReportUseCase.RunReportRequest(
            reportOptions = ReportOptions2(
                xAxis = ReportXAxis.WEEK,
                series = listOf(
                    ReportSeries2(
                        reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION
                    )
                ),
                period = RelativeRangeReportPeriod(ReportTimeRangeUnit.WEEK, 3),
            ),
            accountPersonUid = defaultAccountPersonUid,
            timeZone = TimeZone.UTC,
        )

        val results = runBlocking {
            runReportUseCase(request = request)
        }.results.first()

        assertEquals(3, results.size,
            "result size equals number of weeks of reporting period - 3 weeks")

        (0 until 3).forEach { weekNum ->
            val firstDayOfWeek = Instant.fromEpochMilliseconds(
                request.reportOptions.period.periodStartMillis(request.timeZone)
            ).toLocalDateTime(request.timeZone)
                .date.plus(DatePeriod(days = weekNum * 7))
            val row = results.firstOrNull { it.xAxis == firstDayOfWeek.toString() }


            assertEquals(
                expected = (defaultDurationPerStatement * defaultNumStatementsPerDay * 7).toDouble(),
                actual = row?.yAxis ?: -1f,
                message = "week $weekNum has expected total duration"
            )
        }
    }

    @Test
    fun givenStatementsInDatabase_whenDurationPerMonthQueried_thenReturnsExpectedNumOfResults() {
        val numDaysStatements = 90

        insertStatementsPerDay(
            numDays = numDaysStatements,
        )
        grantLearningRecordViewSystemPermission()

        val reportNumMonths = 3
        val request = RunReportUseCase.RunReportRequest(
            reportOptions = ReportOptions2(
                xAxis = ReportXAxis.MONTH,
                series = listOf(
                    ReportSeries2(
                        reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION
                    )
                ),
                period = RelativeRangeReportPeriod(ReportTimeRangeUnit.MONTH, reportNumMonths),
            ),
            accountPersonUid = defaultAccountPersonUid,
            timeZone = TimeZone.UTC,
        )

        val results = runBlocking {
            runReportUseCase(request = request)
        }.results.first()

        assertEquals(reportNumMonths, results.size,
            "result size equals number of weeks of reporting period - 3 months")
        assertTrue(results.all { it.xAxis.endsWith("01") },
            "Report by month xAxis should always end with 01 (e.g. first of month)")
    }

    @Test
    fun givenStatementsInDatabase_whenDurationPerYearQueried_thenReturnsExpectedNumOfResults() {
        val reportNumYears = 2
        val numDaysStatements = 365 * reportNumYears

        insertStatementsPerDay(
            numDays = numDaysStatements,
        )
        grantLearningRecordViewSystemPermission()

        val request = RunReportUseCase.RunReportRequest(
            reportOptions = ReportOptions2(
                xAxis = ReportXAxis.YEAR,
                series = listOf(
                    ReportSeries2(
                        reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION
                    )
                ),
                period = RelativeRangeReportPeriod(ReportTimeRangeUnit.YEAR, reportNumYears),
            ),
            accountPersonUid = defaultAccountPersonUid,
            timeZone = TimeZone.UTC,
        )

        val results = runBlocking {
            runReportUseCase(request = request)
        }.results.first()

        assertEquals(reportNumYears, results.size,
            "result size equals number of weeks of reporting period - 2 years")
        assertTrue(results.all { it.xAxis.endsWith("01-01") },
            "Report by year xAxis should always end with 01-01 (e.g. first day of the year)")
    }

}