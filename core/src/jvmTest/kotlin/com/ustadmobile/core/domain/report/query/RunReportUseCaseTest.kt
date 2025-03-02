package com.ustadmobile.core.domain.report.query

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.report.model.RelativeReportTimeRange
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportTimeRangeOption
import com.ustadmobile.core.domain.report.model.ReportTimeRangeUnit
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.door.DatabaseBuilder
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

class RunReportUseCaseTest {

    private lateinit var db: UmAppDatabase

    private lateinit var runReportUseCase: RunReportUseCase

    private val fromTimeEpoch = Clock.System.now().toLocalDateTime(
        TimeZone.UTC
    ).let {
        LocalDateTime(it.date.minus(DatePeriod(days = 3)), it.time)
    }.toInstant(TimeZone.UTC).toEpochMilliseconds()

    @BeforeTest
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite::memory:", 1L)
            .build()
        runReportUseCase = RunReportUseCaseDatabaseImpl(db, GenerateReportQueriesUseCase())
    }

    private val defaultNumStatementsPerDay = 2
    private val defaultDurationPerStatement = 2_000L
    private val defaultNumDays = 3

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


    @Test
    fun givenStatementsInDatabase_whenDurationPerDayQueried_thenResultsAsExpected() {
        insertStatementsPerDay()
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
                        timeRange = ReportTimeRangeOption.LAST_WEEK.timeRange,
                    ),
                    accountPersonUid = 1L,
                )
            )
        }.results.first()

        assertEquals(7, results.size,
            "result size equals number of days of reporting period - LAST_WEEK - 7 days")

        (0 until defaultNumDays).forEach { day ->
            val localDate = Instant.fromEpochMilliseconds(fromTimeEpoch + (day * (MS_PER_HOUR * 24)))
                .toLocalDateTime(TimeZone.UTC).date

            assertEquals(
                expected = (defaultDurationPerStatement * defaultNumStatementsPerDay).toDouble(),
                actual = results.find { it.xAxis == localDate.toString() }!!.yAxis,
                message = "day $day has expected total duration"
            )
        }
    }

    //@Test
    fun givenStatementsInDatabase_whenDurationPerWeekQueried_thenResultsAsExpected() {
        val numDaysStatements = 21
        insertStatementsPerDay(
            numDays = numDaysStatements,
        )

        val results = runBlocking {
            runReportUseCase(
                request = RunReportUseCase.RunReportRequest(
                    reportOptions = ReportOptions2(
                        xAxis = ReportXAxis.WEEK,
                        series = listOf(
                            ReportSeries2(
                                reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION
                            )
                        ),
                        timeRange = RelativeReportTimeRange(ReportTimeRangeUnit.WEEK, 3),
                    ),
                    accountPersonUid = 1L,
                )
            )
        }.results.first()

        assertEquals(3, results.size,
            "result size equals number of weeks of reporting period - 3 weeks")

        (0 until 3).forEach { week ->
            val firstDayOfWeek = Instant.fromEpochMilliseconds(fromTimeEpoch)
                .toLocalDateTime(TimeZone.UTC).let { localDateTime ->
                    LocalDateTime(localDateTime.date.plus(DatePeriod(days =  (week * 7))), localDateTime.time)
                }

            assertEquals(
                expected = (defaultDurationPerStatement * defaultNumStatementsPerDay * 7).toDouble(),
                actual = results.find { it.xAxis == firstDayOfWeek.toString() }!!.yAxis,
                message = "week $week has expected total duration"
            )
        }
    }
}