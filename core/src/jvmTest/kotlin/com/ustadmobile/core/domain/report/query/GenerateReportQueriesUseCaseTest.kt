package com.ustadmobile.core.domain.report.query

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateReportQueriesUseCaseTest {

    private lateinit var db: UmAppDatabase

    // Sat Feb 01 2025 09:24:32 GMT+0000
    private val fromTimeEpoch = Clock.System.now().toLocalDateTime(
        TimeZone.currentSystemDefault()
    ).let {
        LocalDateTime(it.date.minus(DatePeriod(days = 3)), it.time)
    }.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    @BeforeTest
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite::memory:", 1L)
            .build()
    }

    private val defaultNumStatementsPerDay = 2
    private val defaultDurationPerStatement = 2_000L
    private val defaultNumDays = 3


    fun insertStatementsPerDay(
        numStatementsPerDay: Int = defaultNumStatementsPerDay,
        durationPerStatement: Long = defaultDurationPerStatement,
        numDays: Int = defaultNumDays,
    ) {
        val statementList = (0 until numDays).flatMap { day ->
            (1..numStatementsPerDay).map {
                val statementUid = uuid4()
                StatementEntity(
                    statementIdHi = statementUid.mostSignificantBits,
                    statementIdLo = statementUid.leastSignificantBits,
                    timestamp = fromTimeEpoch + (day * (MS_PER_HOUR * 24)),
                    resultDuration = durationPerStatement,
                )
            }
        }

        runBlocking {
            db.statementDao().insertOrIgnoreListAsync(statementList)
        }
    }


    @Test
    fun givenStatementsInDatabase_whenDurationPerDayQueried_thenResultsAsExpected() {
        insertStatementsPerDay()
        val out = GenerateReportQueriesUseCase().invoke(
            reportOptions = ReportOptions2(
                xAxis = ReportXAxis.DAY,
                series = listOf(
                    ReportSeries2(
                        reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION
                    )
                )
            ),
            dbType = DoorDbType.SQLITE
        )

        val query = SimpleDoorQuery(out[0].sql, out[0].params)

        val results = runBlocking {
            db.statementDao().runReportQuery(query)
        }

        assertEquals(defaultNumDays, results.size, "results size equals number of days")
        assertTrue(
            results.all { it.yAxis == (defaultDurationPerStatement * defaultNumStatementsPerDay).toDouble() },
            "all results have expected total duration per day"
        )

        (0 until defaultNumDays).forEach { day ->
            val localDate = Instant.fromEpochMilliseconds(fromTimeEpoch + (day * (MS_PER_HOUR * 24)))
                .toLocalDateTime(TimeZone.UTC).date
            localDate.toString()

            assertEquals(
                expected = (defaultDurationPerStatement * defaultNumStatementsPerDay).toDouble(),
                actual = results.find { it.xAxis == localDate.toString() }!!.yAxis,
                message = "day $day has expected total duration"
            )
        }
    }


    @Test
    fun givenStatementsAndPersonInDatabase_whenDurationByGenderQueried_thenResultsAsExpected() {

    }
}