package com.ustadmobile.core.domain.report.query

import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.report.model.RelativeRangeReportPeriod
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportPeriodOption
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportTimeRangeUnit
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.lib.db.entities.SystemPermission
import com.ustadmobile.util.test.ext.DEFAULT_DURATION_PER_STATEMENT
import com.ustadmobile.util.test.ext.DEFAULT_NUM_DAYS
import com.ustadmobile.util.test.ext.DEFAULT_NUM_STATEMENTS_PER_DAY
import com.ustadmobile.util.test.ext.DEFAULT_STATEMENT_CLAZZ_UID
import com.ustadmobile.util.test.ext.insertStatementsPerDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
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

    private val defaultAccountPersonUid = 1L

    private val defaultStatementClazzUid = 42L

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
        runBlocking { db.insertStatementsPerDay() }
        grantLearningRecordViewSystemPermission()

        val results = runBlocking {
            runReportUseCase(
                request = RunReportUseCase.RunReportRequest(
                    reportUid = 42L,
                    reportOptions = ReportOptions2(
                        xAxis = ReportXAxis.DAY,
                        series = listOf(
                            ReportSeries2(
                                reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION,
                                // Explicitly disable subgrouping
                                reportSeriesSubGroup = null
                            )
                        ),
                        period = ReportPeriodOption.LAST_WEEK.period,
                    ),
                    accountPersonUid = defaultAccountPersonUid,
                    timeZone = TimeZone.UTC,
                )
            ).first()
        }

        // Get the first series results (since we only have one series)
        val seriesResults = results.results.first()

        // Verify we have exactly 7 days of data
        assertEquals(7, seriesResults.size,
            "result size equals number of days of reporting period - LAST_WEEK - 7 days")

        (0 until DEFAULT_NUM_DAYS).forEach { dayIndex ->
            val localDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
                .minus(DatePeriod(days = dayIndex))

            assertEquals(
                expected = (DEFAULT_DURATION_PER_STATEMENT * DEFAULT_NUM_STATEMENTS_PER_DAY).toDouble(),
                actual = seriesResults.find { it.xAxis == localDate.toString() }!!.yAxis,
                message = "day $dayIndex has expected total duration"
            )
        }
    }

    @Test
    fun givenStatementsInDatabase_whenDurationPerWeekQueried_thenResultsAsExpected() {
        val numWeeks = 3
        val numDaysStatements = numWeeks * 7
        runBlocking { db.insertStatementsPerDay(numDays = numDaysStatements) }
        grantLearningRecordViewSystemPermission()

        val request = RunReportUseCase.RunReportRequest(
            reportUid = 42L,
            reportOptions = ReportOptions2(
                xAxis = ReportXAxis.WEEK,
                series = listOf(
                    ReportSeries2(
                        reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION,
                        reportSeriesSubGroup = null
                    )
                ),
                period = RelativeRangeReportPeriod(ReportTimeRangeUnit.WEEK, 3),
            ),
            accountPersonUid = defaultAccountPersonUid,
            timeZone = TimeZone.UTC,
        )

        val results = runBlocking {
            runReportUseCase(request = request).first()
        }.results.first()

        assertEquals(numWeeks, results.size,
            "result size equals number of weeks of reporting period - $numWeeks weeks")

        (0 until numWeeks).forEach { weekNum ->
            val firstDayOfWeek = Instant.fromEpochMilliseconds(
                request.reportOptions.period.periodStartMillis(request.timeZone)
            ).toLocalDateTime(request.timeZone)
                .date.plus(DatePeriod(days = weekNum * 7))
            val row = results.firstOrNull { it.xAxis == firstDayOfWeek.toString() }

            assertEquals(
                expected = (DEFAULT_DURATION_PER_STATEMENT * DEFAULT_NUM_STATEMENTS_PER_DAY * 7).toDouble(),
                actual = row?.yAxis ?: -1.0,
                message = "week $weekNum has expected total duration"
            )
        }
    }

    @Test
    fun givenStatementsInDatabase_whenDurationPerMonthQueried_thenReturnsExpectedNumOfResults() {
        val numDaysStatements = 90

        runBlocking { db.insertStatementsPerDay(numDays = numDaysStatements) }
        grantLearningRecordViewSystemPermission()

        val reportNumMonths = 3
        val request = RunReportUseCase.RunReportRequest(
            reportUid = 42L,
            reportOptions = ReportOptions2(
                xAxis = ReportXAxis.MONTH,
                series = listOf(
                    ReportSeries2(
                        reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION,
                        reportSeriesSubGroup = null
                    )
                ),
                period = RelativeRangeReportPeriod(ReportTimeRangeUnit.MONTH, reportNumMonths),
            ),
            accountPersonUid = defaultAccountPersonUid,
            timeZone = TimeZone.UTC,
        )

        val results = runBlocking {
            runReportUseCase(request = request).first()
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

        runBlocking { db.insertStatementsPerDay(numDays = numDaysStatements) }
        grantLearningRecordViewSystemPermission()

        val request = RunReportUseCase.RunReportRequest(
            reportUid = 42L,
            reportOptions = ReportOptions2(
                xAxis = ReportXAxis.YEAR,
                series = listOf(
                    ReportSeries2(
                        reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION,
                        reportSeriesSubGroup = null
                    )
                ),
                period = RelativeRangeReportPeriod(ReportTimeRangeUnit.YEAR, reportNumYears),
            ),
            accountPersonUid = defaultAccountPersonUid,
            timeZone = TimeZone.UTC,
        )

        val results = runBlocking {
            runReportUseCase(request = request).first()
        }.results.first()

        assertEquals(reportNumYears, results.size,
            "result size equals number of weeks of reporting period - 2 years")
        assertTrue(results.all { it.xAxis.endsWith("01-01") },
            "Report by year xAxis should always end with 01-01 (e.g. first day of the year)")
    }

    @Test
    fun givenTeacherHasLearningRecordPermissionForClazz_whenQueryRuns_thenOnlyOwnClazzIsIncluded() {
        val teachersClazzUid = 43L

        // Half of statements will be in the clazzUid for the teacher, half not
        runBlocking {
            db.insertStatementsPerDay(
                statementClazzUid = {
                    if(it.mod(2) == 0)
                        teachersClazzUid
                    else
                        DEFAULT_STATEMENT_CLAZZ_UID
                }
            )
        }

        runBlocking {
            // Grant teacher permissions for their class
            db.coursePermissionDao().upsertAsync(
                CoursePermission(
                    cpToEnrolmentRole = ClazzEnrolment.ROLE_TEACHER,
                    cpClazzUid = teachersClazzUid,
                    cpPermissionsFlag = CoursePermission.TEACHER_DEFAULT_PERMISSIONS,
                )
            )

            // Enroll teacher in their class
            db.clazzEnrolmentDao().insertListAsync(
                listOf(
                    ClazzEnrolment(
                        clazzUid = teachersClazzUid,
                        personUid = defaultAccountPersonUid,
                        role = ClazzEnrolment.ROLE_TEACHER
                    )
                )
            )
        }

        val results = runBlocking {
            runReportUseCase(
                request = RunReportUseCase.RunReportRequest(
                    reportUid = 42L,
                    reportOptions = ReportOptions2(
                        xAxis = ReportXAxis.DAY,
                        series = listOf(
                            ReportSeries2(
                                reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION,
                                reportSeriesSubGroup = null
                            )
                        ),
                        period = ReportPeriodOption.LAST_WEEK.period,
                    ),
                    accountPersonUid = defaultAccountPersonUid,
                    timeZone = TimeZone.UTC,
                )
            ).first()
        }.results.first()

        // Verify we got results only for teacher's class
        (0 until DEFAULT_NUM_DAYS).forEach { dayIndex ->
            val localDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
                .minus(DatePeriod(days = dayIndex))

            assertEquals(
                expected = (DEFAULT_DURATION_PER_STATEMENT * (DEFAULT_NUM_STATEMENTS_PER_DAY/2)).toDouble(),
                actual = results.find { it.xAxis == localDate.toString() }?.yAxis ?: 0.0,
                message = "day $dayIndex has expected total duration"
            )
        }
    }

    @Test
    fun givenAllReportOptionCombinations_whenRun_thenShouldNotThrowException() {
        runBlocking { db.insertStatementsPerDay() }
        grantLearningRecordViewSystemPermission()

        runBlocking {
            ReportSeriesYAxis.entries.forEach { yAxis ->
                ReportXAxis.entries.forEach { xAxis ->
                    try {
                        runReportUseCase(
                            request = RunReportUseCase.RunReportRequest(
                                reportUid = 42L,
                                reportOptions = ReportOptions2(
                                    xAxis = xAxis,
                                    series = listOf(
                                        ReportSeries2(
                                            reportSeriesYAxis = yAxis
                                        )
                                    ),
                                    period = ReportPeriodOption.LAST_WEEK.period,
                                ),
                                accountPersonUid = defaultAccountPersonUid,
                                timeZone = TimeZone.UTC,
                            )
                        )
                    }catch(e: Throwable) {
                        println("Exception running report yAxis=$yAxis xAxis=$xAxis")
                        throw e
                    }
                }
            }
        }
    }

    @Test
    fun givenReportOptionsWithSubgroup_whenRun_thenResultsAsExpected() {
        runBlocking {
            db.insertStatementsPerDay(
                statementClazzUid = {
                    defaultStatementClazzUid + it.mod(2)
                }
            )
        }
        grantLearningRecordViewSystemPermission()

        val results = runBlocking {
            runReportUseCase(
                request = RunReportUseCase.RunReportRequest(
                    reportUid = 42L,
                    reportOptions = ReportOptions2(
                        xAxis = ReportXAxis.DAY,
                        series = listOf(
                            ReportSeries2(
                                reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION,
                                reportSeriesSubGroup = ReportXAxis.CLASS,
                            )
                        ),
                        period = ReportPeriodOption.LAST_WEEK.period,
                    ),
                    accountPersonUid = defaultAccountPersonUid,
                    timeZone = TimeZone.UTC,
                )
            ).first()
        }.results.first()

        //When using subgrouping, for each xAxis day, there should be two results (one per clazzUid value).
        (0 until DEFAULT_NUM_DAYS).forEach { dayIndex ->
            listOf(DEFAULT_STATEMENT_CLAZZ_UID, DEFAULT_STATEMENT_CLAZZ_UID + 1).forEach { clazzUid ->
                val localDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
                    .minus(DatePeriod(days = dayIndex))

                assertEquals(
                    expected = (DEFAULT_DURATION_PER_STATEMENT * (DEFAULT_NUM_STATEMENTS_PER_DAY/2)).toDouble(),
                    actual = results.find {
                        it.xAxis == localDate.toString() && it.subgroup == clazzUid.toString()
                    }!!.yAxis,
                    message = "day $dayIndex has expected total duration"
                )
            }
        }
    }

    @Test
    fun givenReportIsFresh_whenRunAgain_thenCacheResultReturned() {
        runBlocking { db.insertStatementsPerDay() }
        grantLearningRecordViewSystemPermission()
        val runReportRequest = RunReportUseCase.RunReportRequest(
            reportUid = 42L,
            reportOptions = ReportOptions2(
                xAxis = ReportXAxis.DAY,
                series = listOf(
                    ReportSeries2(
                        reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION,
                        reportSeriesSubGroup = null
                    )
                ),
                period = ReportPeriodOption.LAST_WEEK.period,
            ),
            accountPersonUid = defaultAccountPersonUid,
            timeZone = TimeZone.UTC,
        )

        val results = runBlocking {
            runReportUseCase(
                request = runReportRequest
            ).first()
        }

        Thread.sleep(1000)

        val cachedResults = runBlocking {
            runReportUseCase(
                request = runReportRequest
            ).first()
        }

        assertEquals(7, cachedResults.results.first().size,
            "result size equals number of days of reporting period - LAST_WEEK - 7 days")
        assertTrue(cachedResults.age > 0, "Cached results age > 0")
        assertEquals(0, results.age)
    }

}