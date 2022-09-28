package com.ustadmobile.sharedse.xapi

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.xapi.ContextActivity
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.core.contentformats.xapi.ContextDeserializer
import com.ustadmobile.core.contentformats.xapi.StatementDeserializer
import com.ustadmobile.core.contentformats.xapi.StatementSerializer
import com.ustadmobile.core.db.ext.preload
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStatementEndpointImpl
import com.ustadmobile.port.sharedse.impl.http.XapiStatementResponder
import com.ustadmobile.port.sharedse.impl.http.XapiStatementResponder.Companion.URI_PARAM_ENDPOINT
import com.ustadmobile.util.test.checkJndiSetup
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.json.*
import okhttp3.OkHttpClient
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.naming.InitialContext
import kotlin.random.Random
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import io.ktor.client.plugins.contentnegotiation.*
import kotlinx.coroutines.runBlocking

class TestXapiStatementResponder {

    private lateinit var mockUriResource: RouterNanoHTTPD.UriResource
    private lateinit var mockSession: NanoHTTPD.IHTTPSession
    private var db: UmAppDatabase? = null

    val context = Any()

    lateinit var accountManager: UstadAccountManager

    lateinit var di: DI

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Before
    @Throws(IOException::class)
    fun setup() {
        checkJndiSetup()
        val endpointScope = EndpointScope()
        di = DI {
            bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
                NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())
            }

            bind<UstadMobileSystemImpl>() with singleton {
                spy(UstadMobileSystemImpl(XmlPullParserFactory.newInstance(),
                    temporaryFolder.newFolder()))
            }
            bind<UstadAccountManager>() with singleton {
                UstadAccountManager(instance(), Any(), di)
            }
            bind<Gson>() with singleton {
                val builder = GsonBuilder()
                builder.registerTypeAdapter(Statement::class.java, StatementSerializer())
                builder.registerTypeAdapter(Statement::class.java, StatementDeserializer())
                builder.registerTypeAdapter(ContextActivity::class.java, ContextDeserializer())
                builder.create()
            }

            bind<OkHttpClient>() with singleton {
                OkHttpClient()
            }

            bind<HttpClient>() with singleton {
                HttpClient(OkHttp) {
                    install(ContentNegotiation)
                    install(HttpTimeout)
                    engine {
                        preconfigured = instance()
                    }
                }
            }

            bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                val nodeIdAndAuth: NodeIdAndAuth = instance()
                spy(DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite:build/tmp/$dbName.sqlite")
                    .addSyncCallback(nodeIdAndAuth)
                    .build()
                    .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
                    .also { runBlocking { it.preload() } })
            }

            bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope).singleton {
                val nodeIdAndAuth: NodeIdAndAuth = instance()
                spy(instance<UmAppDatabase>(tag = DoorTag.TAG_DB).asRepository(repositoryConfig(Any(),
                    context.url, nodeIdAndAuth.nodeId, nodeIdAndAuth.auth, instance(), instance())))
            }

            registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }

            bind<XapiStatementEndpoint>() with scoped(endpointScope).singleton {
                XapiStatementEndpointImpl(context, di)
            }
        }


        accountManager = di.direct.instance()
        db = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)

        mockUriResource = mock<RouterNanoHTTPD.UriResource> {
            on { initParameter(0, DI::class.java) }.thenReturn(di)
        }

    }

    @Test
    @Throws(IOException::class)
    fun givenValidPostRequest_whenDataInQueryParamString_thenDbShouldBeUpdated() {
        val tmpFile = temporaryFolder.newFile("statement.json")
        javaClass.getResourceAsStream("/com/ustadmobile/port/sharedse/fullstatement")
            .writeToFile(tmpFile)

        val contentEntryUid = 1234L

        val clazzUid = 10001L

        mockSession = mock {
            on { method }.thenReturn(NanoHTTPD.Method.POST)
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/xapi/$contentEntryUid/$clazzUid")
            on { parseBody(any()) }.doAnswer {
                val map = it.arguments[0] as MutableMap<String, String>
                map["postData"] = tmpFile.absolutePath
                Unit
            }
        }

        val responder = XapiStatementResponder()
        val response = responder.post(mockUriResource,
                mutableMapOf(URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl,
                        XapiStatementResponder.URLPARAM_CONTENTENTRYUID to contentEntryUid.toString(),
                        XapiStatementResponder.URLPARAM_CLAZZUID to clazzUid.toString()), mockSession)

        Assert.assertEquals(NanoHTTPD.Response.Status.OK, response.status)
        val statement = db!!.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement!!.statementId)
        val xObject = db!!.xObjectDao.findByXobjectUid(statement.xObjectUid)
        Assert.assertNotNull("Joined XObject is not null", xObject)
        Assert.assertEquals("Statement is associated with expected contentEntryUid",
                contentEntryUid, xObject!!.objectContentEntryUid)
        Assert.assertEquals("Statement is associated with expected clazzUid",
                clazzUid, statement.statementClazzUid)
        Assert.assertEquals("ContentEntry itself has contentEntryUid set", contentEntryUid,
                statement?.statementContentEntryUid)
    }

    @Test
    @Throws(IOException::class)
    fun givenValidPutRequest_whenDataInContentMap_thenDbShouldBeUpdated() {

        val tmpFile = temporaryFolder.newFile("testStatement")
        javaClass.getResourceAsStream("/com/ustadmobile/port/sharedse/fullstatement")
            .writeToFile(tmpFile)

        val contentEntryUid = 1234L

        val clazzUid = 10002L

        mockSession = mock {
            on { method }.thenReturn(NanoHTTPD.Method.PUT)
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/xapi/$contentEntryUid/${clazzUid}")
            on { parseBody(any()) }.doAnswer {
                val map = it.arguments[0] as MutableMap<String, String>
                map["content"] = tmpFile.absolutePath
                Unit
            }
        }

        val responder = XapiStatementResponder()
        val response = responder.put(mockUriResource,
                mutableMapOf(URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl,
                        XapiStatementResponder.URLPARAM_CONTENTENTRYUID to contentEntryUid.toString(),
                        XapiStatementResponder.URLPARAM_CLAZZUID to clazzUid.toString()), mockSession)

        Assert.assertEquals(NanoHTTPD.Response.Status.NO_CONTENT, response.status)
        val statement = db!!.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement!!.statementId)

        val xObject = db!!.xObjectDao.findByXobjectUid(statement.xObjectUid)
        Assert.assertNotNull("Joined XObject is not null", xObject)
        Assert.assertEquals("Statement is associated with expected contentEntryUid",
                1234L, xObject!!.objectContentEntryUid)
        Assert.assertEquals("Statement is associated with expected clazzUid",
                clazzUid, statement.statementClazzUid)
    }

    @Test
    @Throws(IOException::class)
    fun givenAValidPutRequest_whenPutRequestHasStatementIdParam_thenShouldUpdateDb() {

        val tmpFile = temporaryFolder.newFile("testStatement")
        javaClass.getResourceAsStream("/com/ustadmobile/port/sharedse/fullstatement")
            .writeToFile(tmpFile)

        val contentEntryUid = 1234L

        val clazzUid = 1023L

        mockSession = mock {
            on { method }.thenReturn(NanoHTTPD.Method.PUT)
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/xapi/$contentEntryUid/$clazzUid/")
            on { parameters }.thenReturn(mapOf("statementId"  to listOf(URLEncoder.encode("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", StandardCharsets.UTF_8.toString()))))
            on { parseBody(any()) }.doAnswer {
                val map = it.arguments[0] as MutableMap<String, String>
                map["content"] = tmpFile.absolutePath
                Unit
            }
        }

        val responder = XapiStatementResponder()
        val response = responder.put(mockUriResource,
                mutableMapOf(URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl,
                        XapiStatementResponder.URLPARAM_CONTENTENTRYUID to contentEntryUid.toString(),
                        XapiStatementResponder.URLPARAM_CLAZZUID to clazzUid.toString()), mockSession)

        Assert.assertEquals(NanoHTTPD.Response.Status.NO_CONTENT, response.status)
        val statement = db!!.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement!!.statementId)
    }

}
