package com.ustadmobile.core.domain.xapi.formatresponse

import app.cash.turbine.test
import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.StoreActivitiesUseCase
import com.ustadmobile.core.domain.xapi.XapiJson
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xxhash.XXHasher64FactoryCommonJvm
import com.ustadmobile.core.domain.xxhash.XXStringHasherCommonJvm
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatStatementResponseUseCaseTest {

    private lateinit var db: UmAppDatabase

    private lateinit var storeActivitiesUseCase: StoreActivitiesUseCase

    private lateinit var statementResource: XapiStatementResource

    private lateinit var xapiSession: XapiSessionEntity

    private val xxHasher = XXStringHasherCommonJvm()

    private val learningSpace = Endpoint("http://localhost/")

    private lateinit var formatStatementResponseUseCase: FormatStatementResponseUseCase

    private val xapiJson = XapiJson()

    @BeforeTest
    fun setup() {
        val uuid = uuid4()
        xapiSession = XapiSessionEntity(
            xseAccountPersonUid = 42L,
            xseAccountUsername = "user",
            xseClazzUid = 0L,
            xseContentEntryUid = 0L,
            xseRegistrationHi = uuid.mostSignificantBits,
            xseRegistrationLo = uuid.leastSignificantBits,
        )
        db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, "jdbc:sqlite::memory:", nodeId = 1L
        ).build()

        storeActivitiesUseCase = StoreActivitiesUseCase(db, null)

        statementResource = XapiStatementResource(
            db, null,
            xxHasher = xxHasher,
            endpoint = learningSpace,
            xapiJson = xapiJson,
            hasherFactory = XXHasher64FactoryCommonJvm(),
            storeActivitiesUseCase = storeActivitiesUseCase,
        )

        formatStatementResponseUseCase = FormatStatementResponseUseCase(db, null)
    }

    private fun assertFormattedResponseForStatement(
        statementResourcePath: String,
        expectedResponse: String,
    ) = runBlocking {
        val stmtUuid = uuid4()
        statementResource.put(
            statement = xapiJson.json.decodeFromString(
                this::class.java.getResource(statementResourcePath)!!.readText(),
            ),
            statementIdParam = stmtUuid.toString(),
            xapiSession = xapiSession,
        )

        val (statementEntity, activityEntity) = db.statementDao().findByUidWithActivityAsync(
            stmtUuid.mostSignificantBits, stmtUuid.leastSignificantBits
        )!!

        formatStatementResponseUseCase(statementEntity, activityEntity).test {
            val formatted = awaitItem()
            assertEquals(expectedResponse, formatted.string)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenChoiceResponse_whenFormatted_thenResponseWillBeAsExpected() {
        assertFormattedResponseForStatement(
            statementResourcePath = "/com/ustadmobile/core/domain/xapi/multi-choice-statement.json",
            expectedResponse = "Golf Example",
        )
    }

    @Test
    fun givenChoiceWithMultipleResponses_whenFormatted_thenResponseWillBeAsExpected() {
        assertFormattedResponseForStatement(
            statementResourcePath = "/com/ustadmobile/core/domain/xapi/multi-choice-statement-multiple-responses.json",
            expectedResponse = "Golf Example, Tetris Example",
        )
    }

    @Test
    fun givenLikertResponse_whenFormatted_thenWillBeAsExpected() {
        assertFormattedResponseForStatement(
            statementResourcePath = "/com/ustadmobile/core/domain/xapi/likert-response-statement.json",
            expectedResponse = "It's Gonna Change the World",
        )
    }

    @Test
    fun givenSequencingResponse_whenFormatted_thenWillBeAsExpected() {
        assertFormattedResponseForStatement(
            statementResourcePath = "/com/ustadmobile/core/domain/xapi/sequencing-response-statement.json",
            expectedResponse = "Tim, Mike, Ells, Ben",
        )
    }

    @Test
    fun givenMatchingResponse_whenFormatted_thenWillBeAsExpected() {
        assertFormattedResponseForStatement(
            statementResourcePath = "/com/ustadmobile/core/domain/xapi/matching-response-statement.json",
            expectedResponse = "Ben - Duck, Chris - We got Runs, Troy - Van Delay Industries, Freddie - Swift Kick in the Grass",
        )
    }

    @Test
    fun givenPerformanceResponse_whenFormatted_thenWillBeAsExpected() {
        assertFormattedResponseForStatement(
            statementResourcePath = "/com/ustadmobile/core/domain/xapi/performance-response-statement.json",
            expectedResponse = "Net pong matches won: 5, Strokes over par in disc golf at Liberty: 4, Lunch having been eaten: soup",
        )
    }






}