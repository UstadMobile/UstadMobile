package com.ustadmobile.util.test.ext

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

const val DEFAULT_NUM_STATEMENTS_PER_DAY = 2
const val DEFAULT_DURATION_PER_STATEMENT = 2_000L
const val DEFAULT_NUM_DAYS = 3
const val DEFAULT_STATEMENT_CLAZZ_UID = 42L

data class StatementsInsertedInfo(
    val statements: List<StatementEntity>,
)

/**
 * Insert statements that are used for report tests.
 */
suspend fun UmAppDatabase.insertStatementsPerDay(
    numStatementsPerDay: Int = DEFAULT_NUM_STATEMENTS_PER_DAY,
    durationPerStatement: Long = DEFAULT_DURATION_PER_STATEMENT,
    numDays: Int = DEFAULT_NUM_DAYS,
    statementClazzUid: (index: Int) -> Long = { DEFAULT_STATEMENT_CLAZZ_UID },
): StatementsInsertedInfo {
    val today = Clock.System.now().toLocalDateTime(TimeZone.UTC)

    val statementList = (0 until numDays).flatMap { dayIndex ->
        //Adding 24 hours does not always get to the same time next day e.g. when daylight
        // savings time changes. Use LocalDateTime to workaround this.
        val timestamp = LocalDateTime(
            today.date.minus(DatePeriod(days = dayIndex)), today.time
        ).toInstant(TimeZone.UTC)

        (1..numStatementsPerDay).map { statementNum ->
            val statementUid = uuid4()
            StatementEntity(
                statementIdHi = statementUid.mostSignificantBits,
                statementIdLo = statementUid.leastSignificantBits,
                timestamp = timestamp.toEpochMilliseconds(),
                resultDuration = durationPerStatement,
                statementClazzUid = statementClazzUid(statementNum),
            )
        }
    }


    statementDao().insertOrIgnoreListAsync(statementList)
    return StatementsInsertedInfo(statementList)
}
