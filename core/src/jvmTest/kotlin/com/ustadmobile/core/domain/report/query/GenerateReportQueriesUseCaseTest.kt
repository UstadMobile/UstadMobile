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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateReportQueriesUseCaseTest {

    private lateinit var db: UmAppDatabase

    // Sat Feb 01 2025 09:24:32 GMT+0000
    private val fromTimeEpoch = 1738401872000L

    @BeforeTest
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite::memory:", 1L)
            .build()
    }

    @Test
    fun givenStatementsInDatabase_whenDurationPerDayQueried_thenResultsAsExpected() {
        val numStatementsPerDay = 2
        val durationPerStatement = 2_000L
        val numDays = 3
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

        assertEquals(numDays, results.size, "results size equals number of days")
        assertTrue(
            results.all { it.yAxis == (durationPerStatement * numStatementsPerDay).toDouble() },
            "all results have expected total duration per day"
        )
        (1..numDays).forEach { day ->
            assertEquals(
                expected = (numStatementsPerDay * durationPerStatement).toDouble(),
                actual = results.find { it.xAxis == "0$day/02/2025" }!!.yAxis,
                message = "day $day has expected total duration"
            )
        }
    }

}