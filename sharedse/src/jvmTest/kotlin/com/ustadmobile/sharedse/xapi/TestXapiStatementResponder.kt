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
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.sharedse.contentformats.xapi.ContextDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementSerializer
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStatementEndpointImpl
import com.ustadmobile.port.sharedse.impl.http.XapiStatementResponder
import com.ustadmobile.port.sharedse.impl.http.XapiStatementResponder.Companion.URI_PARAM_ENDPOINT
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.naming.InitialContext

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

            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_REPO) with scoped(endpointScope).singleton {
                spy(instance<UmAppDatabase>(tag = UmAppDatabase.TAG_DB).asRepository<UmAppDatabase>(Any(), context.url, "", defaultHttpClient(), null))
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
    fun givenValidPostRequest_whenDataInQueryParamString_thenDbShouldBeUpdated() {
        val tmpFile = temporaryFolder.newFile("statement.json")
        javaClass.getResourceAsStream("/com/ustadmobile/port/sharedse/fullstatement")
            .writeToFile(tmpFile)

        val contentEntryUid = 1234L

        mockSession = mock {
            on { method }.thenReturn(NanoHTTPD.Method.POST)
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/xapi/$contentEntryUid")
            on { parseBody(any()) }.doAnswer {
                val map = it.arguments[0] as MutableMap<String, String>
                map["postData"] = tmpFile.absolutePath
                Unit
            }
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
    fun givenValidPutRequest_whenDataInContentMap_thenDbShouldBeUpdated() {

        val tmpFile = temporaryFolder.newFile("testStatement")
        javaClass.getResourceAsStream("/com/ustadmobile/port/sharedse/fullstatement")
            .writeToFile(tmpFile)

        val contentEntryUid = 1234L

        mockSession = mock {
            on { method }.thenReturn(NanoHTTPD.Method.PUT)
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/xapi/$contentEntryUid")
            on { parseBody(any()) }.doAnswer {
                val map = it.arguments[0] as MutableMap<String, String>
                map["content"] = tmpFile.absolutePath
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

        val tmpFile = temporaryFolder.newFile("testStatement")
        javaClass.getResourceAsStream("/com/ustadmobile/port/sharedse/fullstatement")
            .writeToFile(tmpFile)

        val contentEntryUid = 1234L

        mockSession = mock {
            on { method }.thenReturn(NanoHTTPD.Method.PUT)
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/xapi/$contentEntryUid")
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
                        XapiStatementResponder.URLPARAM_CONTENTENTRYUID to contentEntryUid.toString()), mockSession)

        Assert.assertEquals(NanoHTTPD.Response.Status.NO_CONTENT, response.status)
        val statement = db!!.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement!!.statementId)
    }

}
