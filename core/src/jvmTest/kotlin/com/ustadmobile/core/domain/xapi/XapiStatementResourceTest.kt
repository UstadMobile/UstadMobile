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

    //Note: as per xAPI spec, we should NOT encode default (eg. null) key values
    private val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

    @BeforeTest
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite::memory:", nodeId = 1L)
            .build()
        xapiStatementResource = XapiStatementResource(
            db = db,
            repo = null,
            xxHasher = xxHasher,
            endpoint = endpoint,
            json = json,
            hasherFactory = XXHasher64FactoryCommonJvm(),
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
        assertStatementStoredInDb(stmt.copy(id = id), db, xxHasher)

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
    fun givenStatementWithGroupActor_whenPutCalled_thenShouldBeStored() = runBlocking {
        val xapiSession = XapiSession(
            endpoint = endpoint,
            accountPersonUid = 42L,
            accountUsername = "user",
            clazzUid = 0L,
            contentEntryUid = 0L,
        )

        storeStatementAndAssert("$RESOURCE_PATH/group-statement.json", xapiSession)
        Unit
    }

    companion object {
        const val RESOURCE_PATH = "/com/ustadmobile/core/domain/xapi"
    }

}