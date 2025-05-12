package com.ustadmobile.core.domain.report.query

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.ext.agent
import com.ustadmobile.core.domain.xapi.model.VERB_COMPLETED
import com.ustadmobile.core.domain.xapi.model.VERB_PROGRESSED
import com.ustadmobile.core.domain.xapi.model.XapiActivityStatementObject
import com.ustadmobile.core.domain.xapi.model.XapiContext
import com.ustadmobile.core.domain.xapi.model.XapiObjectType
import com.ustadmobile.core.domain.xapi.model.XapiResult
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xapi.model.XapiVerb
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

const val DEFAULT_NUM_STATEMENTS_PER_DAY = 2
const val DEFAULT_DURATION_PER_STATEMENT = 2_000L
const val DEFAULT_NUM_DAYS = 3

class GenerateTestXapiStatementsUseCase(
    private val db: UmAppDatabase,
    private val endpoint: Endpoint,
    private val xapiStatementResource: XapiStatementResource,
) {
    suspend operator fun invoke(
        contentTitle: String,
        personUid: Long,
        numDays: Int = DEFAULT_NUM_DAYS,
        numStatementsPerDay: Int = DEFAULT_NUM_STATEMENTS_PER_DAY,
        durationPerStatement: Long = DEFAULT_DURATION_PER_STATEMENT,
    ) {
        val contentEntry = db.contentEntryDao().findByTitle(contentTitle).firstOrNull()
            ?: throw IllegalArgumentException("Content entry '$contentTitle' not found")

        val xapiSession = XapiSessionEntity(
            xseUid = db.doorPrimaryKeyManager.nextId(XapiSessionEntity.TABLE_ID),
            xseContentEntryUid = contentEntry.contentEntryUid,
            xseAccountPersonUid = personUid,
            xseStartTime = systemTimeInMillis(),
            xseExpireTime = Long.MAX_VALUE,
            xseLastMod = systemTimeInMillis(),
        ).also { db.xapiSessionEntityDao().insertAsync(it) }

        val today = Clock.System.now()
        val statements = (0 until numDays).flatMap { dayIndex ->
            val date = today.minus(dayIndex, DateTimeUnit.DAY, TimeZone.UTC)
                .toLocalDateTime(TimeZone.UTC).date

            (0 until numStatementsPerDay).map { statementIndex ->
                val isComplete = statementIndex == numStatementsPerDay - 1
                val timestamp = LocalDateTime(
                    date, LocalTime(12, 0, 0)
                ).toInstant(TimeZone.UTC)

                // Inlined statement creation
                XapiStatement(
                    actor = xapiSession.agent(endpoint), verb = XapiVerb(
                        id = if (isComplete) VERB_COMPLETED else VERB_PROGRESSED,
                        display = mapOf("en-US" to if (isComplete) "completed" else "progressed")
                    ), `object` = XapiActivityStatementObject(
                        objectType = XapiObjectType.Activity,
                        id = "${endpoint.url}/content/${contentEntry.contentEntryUid}"
                    ), timestamp = timestamp.toString(), context = XapiContext(
                        platform = "Test Platform",
                        language = "en-US",
                    ), result = XapiResult(
                        completion = isComplete,
                        success = isComplete,
                        duration = "PT${durationPerStatement / 1000}S"
                    )
                )
            }
        }
        xapiStatementResource.post(statements, xapiSession)
    }
}