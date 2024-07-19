package com.ustadmobile.core.domain.xapi

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xxhash.XXHasher64FactoryCommonJvm
import com.ustadmobile.core.domain.xxhash.XXStringHasherCommonJvm
import com.ustadmobile.door.DatabaseBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class XapiStatementResourceTest {

    private lateinit var db: UmAppDatabase

    private val xxHasher = XXStringHasherCommonJvm()

    private val endpoint = Endpoint("http://localhost/")

    private lateinit var xapiStatementResource: XapiStatementResource

    private lateinit var storeActivitiesUseCase: StoreActivitiesUseCase

    private lateinit var defaultXapiSession: XapiSession

    //Note: as per xAPI spec, we should NOT encode default (eg. null) key values
    private val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

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
            json = json,
            hasherFactory = XXHasher64FactoryCommonJvm(),
            storeActivitiesUseCase = storeActivitiesUseCase,
        )
        defaultXapiSession = XapiSession(
            endpoint = endpoint,
            accountPersonUid = 42L,
            accountUsername = "user",
            clazzUid = 0L,
            contentEntryUid = 0L,
            registrationUuid = uuid4().toString(),
        )
    }

    suspend fun storeStatementAndAssert(
        resourcePath: String,
        xapiSession: XapiSession,
    ) : String {
        val id = uuid4().toString()
        val stmtJson = this::class.java.getResource(resourcePath)!!.readText()
        val stmt = json.decodeFromString(XapiStatement.serializer(), stmtJson)
        xapiStatementResource.put(
            statement = stmt,
            statementIdParam = id,
            xapiSession = xapiSession
        )
        assertStatementStoredInDb(stmt.copy(id = id), db, xxHasher, json)

        return id
    }

    @Test
    fun givenStatementPut_whenGetCalled_thenShouldBeRetrieved() = runBlocking {
        val xapiSession = XapiSession(
            endpoint = endpoint,
            accountPersonUid = 42L,
            accountUsername = "user",
            clazzUid = 0L,
            contentEntryUid = 0L,
            registrationUuid = uuid4().toString(),
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