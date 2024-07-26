package com.ustadmobile.core.domain.xapi.state

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.model.XapiAccount
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xapi.model.toEntities
import com.ustadmobile.core.domain.xxhash.XXHasher64FactoryCommonJvm
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.domain.xxhash.XXStringHasherCommonJvm
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.request.iRequestBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class XapiStateUseCaseIntegrationTest {

    private lateinit var db: UmAppDatabase

    private lateinit var storeXapiStateUseCase: StoreXapiStateUseCase

    private lateinit var retrieveXapiStateUseCase: RetrieveXapiStateUseCase

    private lateinit var xapiSession: XapiSession

    private lateinit var xapiAgent: XapiAgent

    private lateinit var xxStringHasher: XXStringHasher

    private val json = Json { encodeDefaults = false }

    val actorPersonUid  = 1L

    @BeforeTest
    fun setup() {
        xxStringHasher = XXStringHasherCommonJvm()
        db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, "jdbc:sqlite::memory:", 1L
        ).build()

        xapiAgent = XapiAgent(
            account = XapiAccount(
                homePage = "https://example.org/",
                name = "username",
            )
        )



        val actorEntities = xapiAgent.toEntities(
            stringHasher = xxStringHasher,
            primaryKeyManager = db.doorPrimaryKeyManager,
            hasherFactory = XXHasher64FactoryCommonJvm(),
            knownActorUidToPersonUidMap = mapOf(
                xapiAgent.identifierHash(xxStringHasher) to actorPersonUid
            )
        )



        runBlocking {
            db.actorDao().insertOrIgnoreListAsync(listOf(actorEntities.actor))
        }

        xapiSession = XapiSession(
            endpoint = Endpoint("https://example.org/"),
            accountPersonUid = 1L,
            accountUsername = "username",
            clazzUid = 0L,
            registrationUuid = uuid4().toString(),
        )

        storeXapiStateUseCase = StoreXapiStateUseCase(
            db = db,
            repo = null,
            json = json,
            xxStringHasher = XXStringHasherCommonJvm(),
            xxHasher64Factory = XXHasher64FactoryCommonJvm()
        )

        retrieveXapiStateUseCase = RetrieveXapiStateUseCase(
            db = db,
            repo = null,
            json = json,
            xxStringHasher = XXStringHasherCommonJvm(),
            xxHasher64Factory = XXHasher64FactoryCommonJvm(),
        )
    }

    @Test
    fun givenStateStored_whenRetrieve_thenShouldMatch() {
        val doc = buildJsonObject {
            put("a", JsonPrimitive("Jane Doe"))
            put("b", JsonPrimitive("John Doe"))
        }

        val activityId = "http://example.org/id"
        val stateId = "aStateId"

        runBlocking {
            val stateParams = XapiStateParams(
                activityId = activityId,
                agent = json.encodeToString(xapiAgent),
                registration = xapiSession.registrationUuid,
                stateId = stateId,
            )

            storeXapiStateUseCase(
                xapiSession = xapiSession,
                xapiStateParams = stateParams,
                method = IHttpRequest.Companion.Method.PUT,
                contentType = "application/json",
                request = iRequestBuilder("http://localhost/xapi/activities/state") {
                    method = IHttpRequest.Companion.Method.PUT
                    header("content-type", "application/json")
                    body(json.encodeToString(doc))
                }
            )

            val retrieveResult = retrieveXapiStateUseCase(
                xapiSession = xapiSession,
                xapiStateParams = stateParams
            ) as RetrieveXapiStateUseCase.TextRetrieveXapiStateResult

            val docParsed = json.decodeFromString(JsonObject.serializer(), retrieveResult.content)

            assertEquals(doc, docParsed)
        }
    }

    @Test
    fun givenExistingStateStored_whenNewStatePosted_thenShouldMerge(){
        val activityId = "http://example.org/id"
        val stateId = "aStateId"

        runBlocking {
            val stateParams = XapiStateParams(
                activityId = activityId,
                agent = json.encodeToString(xapiAgent),
                registration = xapiSession.registrationUuid,
                stateId = stateId,
            )

            val doc1 = buildJsonObject {
                put("a", JsonPrimitive("A"))
                put("b", JsonPrimitive("B"))
            }

            storeXapiStateUseCase(
                xapiSession = xapiSession,
                xapiStateParams = stateParams,
                method = IHttpRequest.Companion.Method.POST,
                contentType = "application/json",
                request = iRequestBuilder("http://localhost/xapi/activities/state") {
                    method = IHttpRequest.Companion.Method.PUT
                    header("content-type", "application/json")
                    body(json.encodeToString(doc1))
                }
            )

            val doc2 = buildJsonObject {
                put("a", JsonPrimitive("A1"))
                put("c", JsonPrimitive("C1"))
            }

            storeXapiStateUseCase(
                xapiSession = xapiSession,
                xapiStateParams = stateParams,
                method = IHttpRequest.Companion.Method.POST,
                contentType = "application/json",
                request = iRequestBuilder("http://localhost/xapi/activities/state") {
                    method = IHttpRequest.Companion.Method.PUT
                    header("content-type", "application/json")
                    body(json.encodeToString(doc2))
                }
            )

            val retrieveResult = retrieveXapiStateUseCase(
                xapiSession = xapiSession,
                xapiStateParams = stateParams
            ) as RetrieveXapiStateUseCase.TextRetrieveXapiStateResult

            val docParsed = json.decodeFromString(JsonObject.serializer(), retrieveResult.content)
            assertEquals("A1", docParsed["a"]!!.jsonPrimitive.content)
            assertEquals("B", docParsed["b"]!!.jsonPrimitive.content)
            assertEquals("C1", docParsed["c"]!!.jsonPrimitive.content)
        }
    }

    @Test
    fun givenBinaryStateStored_whenRetrieved_thenShouldMatch() {
        val activityId = "http://example.org/id"
        val stateId = "aStateId"

        val binaryData = Random.nextBytes(ByteArray(200))
        runBlocking {
            val stateParams = XapiStateParams(
                activityId = activityId,
                agent = json.encodeToString(xapiAgent),
                registration = xapiSession.registrationUuid,
                stateId = stateId,
            )
            storeXapiStateUseCase(
                xapiSession = xapiSession,
                xapiStateParams = stateParams,
                method = IHttpRequest.Companion.Method.PUT,
                contentType = "application/octet-stream",
                request = iRequestBuilder("http://localhost/xapi/activities/state") {
                    method = IHttpRequest.Companion.Method.PUT
                    body(binaryData)
                }
            )

            val retrieveResult = retrieveXapiStateUseCase(
                xapiSession = xapiSession,
                xapiStateParams = stateParams,
            )

            Assert.assertArrayEquals(
                binaryData, (retrieveResult as RetrieveXapiStateUseCase.ByteRetrieveXapiStateResult).content
            )
        }
    }

}