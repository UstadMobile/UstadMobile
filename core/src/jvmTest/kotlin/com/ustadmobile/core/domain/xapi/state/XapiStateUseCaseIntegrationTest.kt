package com.ustadmobile.core.domain.xapi.state

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.model.XapiAccount
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xxhash.XXHasher64FactoryCommonJvm
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.domain.xxhash.XXStringHasherCommonJvm
import com.ustadmobile.door.DatabaseBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class XapiStateUseCaseIntegrationTest {

    private lateinit var db: UmAppDatabase

    private lateinit var storeXapiStateUseCase: StoreXapiStateUseCase

    private lateinit var xapiSession: XapiSession

    private lateinit var xapiAgent: XapiAgent

    private lateinit var xxStringHasher: XXStringHasher

    private val json = Json { encodeDefaults = false }

    @BeforeTest
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, "jdbc:sqlite::memory:", 1L
        ).build()

        xapiAgent = XapiAgent(
            account = XapiAccount(
                homePage = "https://example.org/",
                name = "username",
            )
        )

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

        xxStringHasher = XXStringHasherCommonJvm()
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
            storeXapiStateUseCase(
                xapiSession = xapiSession,
                xapiStateParams = XapiStateParams(
                    activityId = activityId,
                    agent = json.encodeToString(xapiAgent),
                    registration = xapiSession.registrationUuid,
                    stateId = stateId,
                ),
                stateBody = json.encodeToString(doc)
            )

            val stateEntities = db.stateEntityDao().getByParams(
                accountPersonUid = 1L,
                agentActorUid = xapiAgent.identifierHash(xxStringHasher),
                activityUid = xxStringHasher.hash(activityId),
                registrationIdHi = null,
                registrationIdLo = null,
                stateId = stateId,
            )

            assertEquals(doc.keys.size, stateEntities.size)
        }


    }

}