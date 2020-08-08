package com.ustadmobile.sharedse.xapi

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.xapi.ContextActivity
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStateEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.sharedse.contentformats.xapi.ContextDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementSerializer
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStateEndpointImpl
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStatementEndpointImpl
import com.ustadmobile.port.sharedse.impl.http.XapiStateResponder
import com.ustadmobile.port.sharedse.impl.http.XapiStatementResponder
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.util.test.extractTestResourceToFile
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.kodein.di.*
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.naming.InitialContext

class TestXapiStateResponder {

    private lateinit var mockUriResource: RouterNanoHTTPD.UriResource
    private lateinit var mockSession: NanoHTTPD.IHTTPSession
    private var db: UmAppDatabase? = null

    val context = Any()

    lateinit var accountManager: UstadAccountManager

    lateinit var di: DI

    internal var contentMapToken = object : TypeToken<HashMap<String, String>>() {

    }.type

    @Before
    @Throws(IOException::class)
    fun setup() {
        checkJndiSetup()
        val endpointScope = EndpointScope()
        val systemImplSpy = spy(UstadMobileSystemImpl.instance)
        di = DI {
            bind<UstadMobileSystemImpl>() with singleton { systemImplSpy!! }
            bind<UstadAccountManager>() with singleton { UstadAccountManager(instance(), Any(), di) }
            bind<Gson>() with singleton {
                val builder = GsonBuilder()
                builder.registerTypeAdapter(Statement::class.java, StatementSerializer())
                builder.registerTypeAdapter(Statement::class.java, StatementDeserializer())
                builder.registerTypeAdapter(ContextActivity::class.java, ContextDeserializer())
                builder.create()
            }
            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(endpointScope!!).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                spy(UmAppDatabase.getInstance(Any(), dbName).also {
                    it.clearAllTables()
                    it.preload()
                })
            }
            registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }

            bind<XapiStateEndpoint>() with scoped(endpointScope).singleton {
                XapiStateEndpointImpl(context, di)
            }
        }

        accountManager = di.direct.instance()
        db = di.on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_DB)

        mockUriResource = mock<RouterNanoHTTPD.UriResource> {
            on { initParameter(0, DI::class.java) }.thenReturn(di)
        }
    }

    @Test
    @Throws(IOException::class)
    fun testput() {

        val tmpFile = File.createTempFile("testState", "state")
        extractTestResourceToFile("/com/ustadmobile/port/sharedse/state", tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        mockSession = mock {
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/xapi/activities/state")
            on { queryParameterString }.thenReturn(content)
            on { headers }.thenReturn(mapOf("content-type" to "application/json"))
            on { parameters }.thenReturn(
                    mapOf("activityId" to
                            listOf("http://www.example.com/activities/1"),
                            "agent" to listOf(
                                    "{\"objectType\": \"Agent\", \"name\": \"John Smith\", \"account\":{\"name\": \"123\", \"homePage\": \"http://www.example.com/users/\"}}"),
                            "stateId" to listOf("http://www.example.com/states/1")))
        }

        val responder = XapiStateResponder()
        val response = responder.put(mockUriResource,
                mutableMapOf(XapiStatementResponder.URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl), mockSession)

        Assert.assertEquals(NanoHTTPD.Response.Status.NO_CONTENT, response.status)
        val agentEntity = db!!.agentDao.getAgentByAnyId("", "", "123", "http://www.example.com/users/", "")
        val stateEntity = db!!.stateDao.findByStateId("http://www.example.com/states/1", agentEntity!!.agentUid, "http://www.example.com/activities/1", "")
        Assert.assertEquals("http://www.example.com/activities/1", stateEntity!!.activityId)
    }

    @Test
    @Throws(IOException::class)
    fun testPost() {

        val tmpFile = File.createTempFile("testState", "state")
        extractTestResourceToFile("/com/ustadmobile/port/sharedse/state", tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        mockSession = mock {
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/xapi/activities/state")
            on { parseBody(any()) }.doAnswer {
                val map = it.arguments[0] as MutableMap<String, String>
                map["postData"] = content
                Unit
            }
            on { headers }.thenReturn(mapOf("content-type" to "application/json"))
            on { parameters }.thenReturn(
                    mapOf("activityId" to
                            listOf("http://www.example.com/activities/1"),
                            "agent" to listOf(
                                    "{\"objectType\": \"Agent\", \"name\": \"John Smith\", \"account\":{\"name\": \"123\", \"homePage\": \"http://www.example.com/users/\"}}"),
                            "stateId" to listOf("http://www.example.com/states/1")))
        }

        val responder = XapiStateResponder()
        val response = responder.post(mockUriResource,
                mutableMapOf(XapiStatementResponder.URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl), mockSession)

        Assert.assertEquals(NanoHTTPD.Response.Status.NO_CONTENT, response.status)
        val agentEntity = db!!.agentDao.getAgentByAnyId("", "", "123", "http://www.example.com/users/", "")
        val stateEntity = db!!.stateDao.findByStateId("http://www.example.com/states/1", agentEntity!!.agentUid, "http://www.example.com/activities/1", "")
        Assert.assertEquals("http://www.example.com/activities/1", stateEntity!!.activityId)
    }

    @ExperimentalStdlibApi
    @Test
    @Throws(IOException::class)
    fun testAll() {
        val tmpFile = File.createTempFile("testState", "state")
        extractTestResourceToFile("/com/ustadmobile/port/sharedse/state", tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        mockSession = mock {
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/xapi/activities/state")
            on { parseBody(any()) }.doAnswer {
                val map = it.arguments[0] as MutableMap<String, String>
                map["postData"] = content
                Unit
            }
            on { headers }.thenReturn(mapOf("content-type" to "application/json"))
            on { parameters }.thenReturn(
                    mapOf("activityId" to
                            listOf("http://www.example.com/activities/1"),
                            "agent" to listOf(
                                    "{\"objectType\": \"Agent\", \"name\": \"John Smith\", \"account\":{\"name\": \"123\", \"homePage\": \"http://www.example.com/users/\"}}"),
                            "stateId" to listOf("http://www.example.com/states/1")))
        }

        val responder = XapiStateResponder()
        val response = responder.post(mockUriResource,
                mutableMapOf(XapiStatementResponder.URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl), mockSession)

        Assert.assertEquals(NanoHTTPD.Response.Status.NO_CONTENT, response.status)
        val agentEntity = db!!.agentDao.getAgentByAnyId("", "", "123", "http://www.example.com/users/", "")
        val stateEntity = db!!.stateDao.findByStateId("http://www.example.com/states/1", agentEntity!!.agentUid, "http://www.example.com/activities/1", "")
        Assert.assertEquals("http://www.example.com/activities/1", stateEntity!!.activityId)

        val getResponse = responder.get(mockUriResource,
                mutableMapOf(XapiStatementResponder.URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl), mockSession)

        val json = UMIOUtils.readStreamToString(getResponse.data)
        val contentMap = Gson().fromJson<HashMap<String, String>>(json, contentMapToken)
        Assert.assertEquals("Content matches", "Parthenon", contentMap["name"])

        val deleteResponse =  responder.delete(mockUriResource,
                mutableMapOf(XapiStatementResponder.URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl), mockSession)

        Assert.assertEquals(NanoHTTPD.Response.Status.NO_CONTENT, deleteResponse.status)
        val deletedState = db!!.stateDao.findByStateId("http://www.example.com/states/1", agentEntity.agentUid, "http://www.example.com/activities/1", "")
        Assert.assertNull(deletedState)

    }
}
