package com.ustadmobile.core.domain.xapi

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xxhash.XXHasher64FactoryCommonJvm
import com.ustadmobile.core.domain.xxhash.XXStringHasherCommonJvm
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class XapiStatementResourceTest {

    private lateinit var db: UmAppDatabase

    private val xxHasher = XXStringHasherCommonJvm()

    private val endpoint = Endpoint("http://localhost/")

    private lateinit var xapiStatementResource: XapiStatementResource

    private lateinit var storeActivitiesUseCase: StoreActivitiesUseCase

    private lateinit var defaultXapiSession: XapiSessionEntity

    private val xapiJson = XapiJson()

    @BeforeTest
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite::memory:", nodeId = 1L)
            .build()
        storeActivitiesUseCase = StoreActivitiesUseCase(db, null)
        xapiStatementResource = XapiStatementResource(
            db = db,
            repo = null,
            xxHasher = xxHasher,
            endpoint = endpoint,
            xapiJson = xapiJson,
            hasherFactory = XXHasher64FactoryCommonJvm(),
            storeActivitiesUseCase = storeActivitiesUseCase,
        )
        val registrationUuid = uuid4()
        defaultXapiSession = XapiSessionEntity(
            xseAccountPersonUid = 42L,
            xseAccountUsername = "user",
            xseClazzUid = 0L,
            xseContentEntryUid = 0L,
            xseRegistrationHi = registrationUuid.mostSignificantBits,
            xseRegistrationLo = registrationUuid.leastSignificantBits,
        )
    }

    private suspend fun storeStatementAndAssert(
        resourcePath: String,
        xapiSession: XapiSessionEntity,
    ) : String {
        val id = uuid4().toString()
        val stmtJson = this::class.java.getResource(resourcePath)!!.readText()
        val stmt = xapiJson.json.decodeFromString(XapiStatement.serializer(), stmtJson)
        xapiStatementResource.put(
            statement = stmt,
            statementIdParam = id,
            xapiSession = xapiSession
        )
        assertStatementStoredInDb(stmt.copy(id = id), db, xxHasher, xapiJson.json)

        return id
    }

    @Test
    fun givenStatementPut_whenGetCalled_thenShouldBeRetrieved() = runBlocking {
        val uuid = uuid4()
        val xapiSession = XapiSessionEntity(
            xseAccountPersonUid = 42L,
            xseAccountUsername = "user",
            xseClazzUid = 0L,
            xseContentEntryUid = 0L,
            xseRegistrationHi = uuid.mostSignificantBits,
            xseRegistrationLo = uuid.leastSignificantBits,
        )

        val id = storeStatementAndAssert(
            resourcePath = "$RESOURCE_PATH/simple-statement.json",
            xapiSession = xapiSession,
        )

        val retrieved = xapiStatementResource.get(
            xapiSession = xapiSession,
            statementId = id,
        )

        assertEquals(1, retrieved.size)
    }

    @Test
    fun givenStatementWithGroupActorAndExtensions_whenPutCalled_thenShouldBeStored() = runBlocking {
        storeStatementAndAssert("$RESOURCE_PATH/group-statement.json", defaultXapiSession)
        Unit
    }

    @Test
    fun givenStatementWithChoiceActivity_whenStored_thenShouldBeInDb() = runBlocking {
        storeStatementAndAssert("$RESOURCE_PATH/multi-choice-statement.json", defaultXapiSession)
        Unit
    }

    @Test
    fun givenStatementWithObjectAsGroup_whenStored_thenShouldBeInDb() = runBlocking {
        storeStatementAndAssert("$RESOURCE_PATH/statement-with-object-actor.json", defaultXapiSession)
        Unit
    }

    @Test
    fun givenStatementWithObjectAsStatementRef_whenStored_thenShouldBeInDb() = runBlocking {
        storeStatementAndAssert("$RESOURCE_PATH/statement-with-object-statementref.json", defaultXapiSession)
        Unit
    }

    @Test
    fun givenStatementWithObjectAsSubStatement_whenStored_thenShouldBeInDb() = runBlocking {
        storeStatementAndAssert("$RESOURCE_PATH/statement-with-object-substatement.json", defaultXapiSession)
        Unit
    }

    @Test
    fun givenLongStatementWithContextActivities_whenStored_thenShouldBeInDb() = runBlocking {
        storeStatementAndAssert("$RESOURCE_PATH/appendix-a-long-statement.json", defaultXapiSession)
        Unit
    }

    companion object {
        const val RESOURCE_PATH = "/com/ustadmobile/core/domain/xapi"
    }

}