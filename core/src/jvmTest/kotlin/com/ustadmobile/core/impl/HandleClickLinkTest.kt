package com.ustadmobile.core.impl

import com.google.gson.Gson
import com.ustadmobile.core.account.*
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ACTIVE_SESSION_PREFKEY
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ACTIVE_ENDPOINT_PREFKEY
import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ENDPOINTS_WITH_ACTIVE_SESSION
import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.db.waitUntil
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveDbInstance
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.userAtServer
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.IncomingReplicationEvent
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.util.test.ext.insertTestStatementsForReports
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*
import java.io.ByteArrayOutputStream
import javax.naming.InitialContext
import kotlin.random.Random


class HandleClickLinkTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    lateinit var mockSystemImpl: UstadMobileSystemImpl

    val appContext = Any()

    lateinit var mockWebServer: MockWebServer

    lateinit var mockServerUrl: String

    private lateinit var di: DI

    private lateinit var repo: UmAppDatabase

    private lateinit var db: UmAppDatabase

    private lateinit var accountManager: UstadAccountManager
    private val loggedPersonUid:Long = 234568

    private val serverUrl = "http://localhost/dummy/"

    @Before
    fun setup() {
        accountManager = mock{
            on{activeAccount}.thenReturn(UmAccount(loggedPersonUid,"","",serverUrl))
            on { activeEndpoint }.thenReturn(Endpoint(serverUrl))
        }

        val di = DI {
            import(ustadTestRule.diModule)
            bind<UstadAccountManager>(overrides = true) with singleton { accountManager }
        }
        db = di.directActiveDbInstance()
        repo = di.directActiveRepoInstance()

        runBlocking {
            val person = repo.insertPersonAndGroup(Person().apply{
                personUid = loggedPersonUid
                admin = true
                firstNames = "Bob"
                lastName = "Jones"
            })

            repo.grantScopedPermission(person, Role.ALL_PERMISSIONS,
                ScopedGrant.ALL_TABLES, ScopedGrant.ALL_ENTITIES)

            repo.insertTestStatementsForReports()
        }
    }


    @Test
    fun givenNoUserInPrefKeys_whenInitialized_shouldInitGuestAccountOnDefaultServer() {
        Assert.assertEquals(1,1)


        //WIP ..



    }



}