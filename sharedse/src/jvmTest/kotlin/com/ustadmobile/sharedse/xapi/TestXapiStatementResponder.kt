package com.ustadmobile.sharedse.xapi

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.xapi.ContextActivity
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.sharedse.contentformats.xapi.ContextDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementSerializer
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStatementEndpointImpl
import com.ustadmobile.port.sharedse.impl.http.XapiStatementResponder
import com.ustadmobile.port.sharedse.impl.http.XapiStatementResponder.Companion.URI_PARAM_ENDPOINT
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import javax.naming.InitialContext

class TestXapiStatementResponder {

    private lateinit var mockUriResource: RouterNanoHTTPD.UriResource
    private lateinit var mockSession: NanoHTTPD.IHTTPSession
    internal lateinit var httpd: RouterNanoHTTPD
    private var db: UmAppDatabase? = null

    val context = Any()

    lateinit var accountManager: UstadAccountManager

    lateinit var di: DI

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

            bind<XapiStatementEndpoint>() with scoped(endpointScope).singleton {
                XapiStatementEndpointImpl(context, di)
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
    fun givenValidPutRequest_whenDataInQueryParamString_thenDbShouldBeUpdated() {

        val tmpFile = File.createTempFile("testStatement", "statement")
        extractTestResourceToFile("/com/ustadmobile/port/sharedse/fullstatement", tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        val contentEntryUid = 1234L

        mockSession = mock {
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/xapi/$contentEntryUid")
            on { queryParameterString }.thenReturn(content)
        }

        val responder = XapiStatementResponder()
        val response = responder.put(mockUriResource,
                mutableMapOf(URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl,
                        XapiStatementResponder.URLPARAM_CONTENTENTRYUID to contentEntryUid.toString()), mockSession)

        Assert.assertEquals(NanoHTTPD.Response.Status.NO_CONTENT, response.status)
        val statement = db!!.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement!!.statementId)

        val xObject = db!!.xObjectDao.findByXobjectUid(statement.xObjectUid)
        Assert.assertNotNull("Joined XObject is not null", xObject)
        Assert.assertEquals("Statement is associated with expected contentEntryUid",
                1234L, xObject!!.objectContentEntryUid)
    }

    @Test
    @Throws(IOException::class)
    fun givenValidPutRequest_whenDataInContentMap_thenDbShouldBeUpdated() {

        val tmpFile = File.createTempFile("testStatement", "statement")
        extractTestResourceToFile("/com/ustadmobile/port/sharedse/fullstatement", tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        val contentEntryUid = 1234L

        mockSession = mock {
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/xapi/$contentEntryUid")
            on { parseBody(any()) }.doAnswer {
                val map = it.arguments[0] as MutableMap<String, String>
                map["content"] = content
                Unit
            }
        }

        val responder = XapiStatementResponder()
        val response = responder.put(mockUriResource,
                mutableMapOf(URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl,
                        XapiStatementResponder.URLPARAM_CONTENTENTRYUID to contentEntryUid.toString()), mockSession)

        Assert.assertEquals(NanoHTTPD.Response.Status.NO_CONTENT, response.status)
        val statement = db!!.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement!!.statementId)

        val xObject = db!!.xObjectDao.findByXobjectUid(statement.xObjectUid)
        Assert.assertNotNull("Joined XObject is not null", xObject)
        Assert.assertEquals("Statement is associated with expected contentEntryUid",
                1234L, xObject!!.objectContentEntryUid)
    }

    @Test
    @Throws(IOException::class)
    fun givenAValidPutRequest_whenPutRequestHasStatementIdParam_thenShouldUpdateDb() {

        val tmpFile = File.createTempFile("testStatement", "statement")
        extractTestResourceToFile("/com/ustadmobile/port/sharedse/fullstatement", tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))
        println(content)

        val contentEntryUid = 1234L

        mockSession = mock {
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/xapi/$contentEntryUid")
            on { parameters }.thenReturn(mapOf("statementId"  to listOf(URLEncoder.encode("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", StandardCharsets.UTF_8.toString()))))
            on { queryParameterString }.thenReturn(content)
        }

        val responder = XapiStatementResponder()
        val response = responder.put(mockUriResource,
                mutableMapOf(URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl,
                        XapiStatementResponder.URLPARAM_CONTENTENTRYUID to contentEntryUid.toString()), mockSession)

        Assert.assertEquals(NanoHTTPD.Response.Status.NO_CONTENT, response.status)
        val statement = db!!.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement!!.statementId)
    }


    @Test
    @Throws(IOException::class)
    fun givenValidPostRequest_whenDataInQueryParamString_thenDbShouldBeUpdated() {
        val tmpFile = File.createTempFile("testStatement", "statement")
        extractTestResourceToFile("/com/ustadmobile/port/sharedse/fullstatement", tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        val contentEntryUid = 1234L

        mockSession = mock {
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/xapi/$contentEntryUid")
            on { queryParameterString }.thenReturn(content)
        }

        val responder = XapiStatementResponder()
        val response = responder.post(mockUriResource,
                mutableMapOf(URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl,
                        XapiStatementResponder.URLPARAM_CONTENTENTRYUID to contentEntryUid.toString()), mockSession)

        Assert.assertEquals(NanoHTTPD.Response.Status.OK, response.status)
        val statement = db!!.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement!!.statementId)
        val xObject = db!!.xObjectDao.findByXobjectUid(statement.xObjectUid)
        Assert.assertNotNull("Joined XObject is not null", xObject)
        Assert.assertEquals("Statement is associated with expected contentEntryUid",
                contentEntryUid, xObject!!.objectContentEntryUid)
        Assert.assertEquals("ContentEntry itself has contentEntryUid set", contentEntryUid,
                statement?.statementContentEntryUid)
    }


    @Test
    @Throws(IOException::class)
    fun givenAValidStatement_whenPostRequestHasQueryParamsWithMethodisPut_thenShouldReturn204() {

        val tmpFile = File.createTempFile("testStatement", "statement")
        extractTestResourceToFile("/com/ustadmobile/port/sharedse/fullstatement", tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        val contentEntryUid = 1234L

        mockSession = mock {
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/xapi/$contentEntryUid")
            on { queryParameterString }.thenReturn(content)
            on { parameters }.thenReturn(mapOf("method" to listOf("PUT")))
        }

        val responder = XapiStatementResponder()
        val response = responder.post(mockUriResource,
                mutableMapOf(URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl,
                        XapiStatementResponder.URLPARAM_CONTENTENTRYUID to contentEntryUid.toString()), mockSession)

        Assert.assertEquals(NanoHTTPD.Response.Status.NO_CONTENT, response.status)
        val statement = db!!.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement!!.statementId)

    }

}
