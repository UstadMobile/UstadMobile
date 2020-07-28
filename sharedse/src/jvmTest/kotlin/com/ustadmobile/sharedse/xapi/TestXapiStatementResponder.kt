package com.ustadmobile.sharedse.xapi

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.contentformats.xapi.ContextActivity
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.sharedse.contentformats.xapi.ContextDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementSerializer
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStatementEndpointImpl
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.impl.http.XapiStatementResponder
import com.ustadmobile.sharedse.util.UstadTestRule
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.util.test.extractTestResourceToFile
import fi.iki.elonen.router.RouterNanoHTTPD
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
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
import javax.naming.InitialContext

class TestXapiStatementResponder {

    internal lateinit var httpd: RouterNanoHTTPD
    private var appRepo: UmAppDatabase? = null

    val context = Any()

    lateinit var di: DI

    @Before
    @Throws(IOException::class)
    fun setup() {

        val endpointScope = EndpointScope()
        di = DI{
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
            bind<XapiStatementEndpoint>() with singleton {
                XapiStatementEndpointImpl(Endpoint("http://localhost:8087/"), di = di)
            }
        }

        checkJndiSetup()
        appRepo = di.on(Endpoint("http://localhost:8087/")).direct.instance(tag = UmAppDatabase.TAG_DB)

        httpd = EmbeddedHTTPD(0, di)
        httpd.addRoute("/xapi/:contentEntryUid/statements", XapiStatementResponder::class.java, di)
        httpd.start()
    }

    @Test
    @Throws(IOException::class)
    fun testput() {
        val contentEntryUid = 1234L
        val urlString = "http://localhost:" + httpd.listeningPort + "/xapi/$contentEntryUid/statements"

        val tmpFile = File.createTempFile("testStatement", "statement")
        extractTestResourceToFile("/com/ustadmobile/port/sharedse/fullstatement", tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        val httpCon = URL(urlString).openConnection() as HttpURLConnection
        httpCon.doOutput = true
        httpCon.requestMethod = "PUT"
        val out = OutputStreamWriter(
                httpCon.outputStream)
        out.write(content)
        out.close()
        httpCon.connect()

        val code = httpCon.responseCode

        Assert.assertEquals(204, code.toLong())
        val statement = appRepo!!.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement!!.statementId)

        val xObject = appRepo!!.xObjectDao.findByXobjectUid(statement.xObjectUid)
        Assert.assertNotNull("Joined XObject is not null", xObject)
        Assert.assertEquals("Statement is associated with expected contentEntryUid",
                contentEntryUid, xObject!!.objectContentEntryUid)
    }

    @Test
    @Throws(IOException::class)
    fun testPost() {
        val contentEntryUid = 1234L
        val urlString = "http://localhost:" + httpd.listeningPort + "/xapi/$contentEntryUid/statements"

        val tmpFile = File.createTempFile("testStatement", "statement")
        extractTestResourceToFile("/com/ustadmobile/port/sharedse/fullstatement", tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        val httpCon = URL(urlString).openConnection() as HttpURLConnection
        httpCon.doOutput = true
        httpCon.requestMethod = "POST"
        val out = OutputStreamWriter(
                httpCon.outputStream)
        out.write(content)
        out.close()
        httpCon.connect()

        val code = httpCon.responseCode

        Assert.assertEquals(200, code.toLong())
        val statement = appRepo!!.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement!!.statementId)
        val xObject = appRepo!!.xObjectDao.findByXobjectUid(statement.xObjectUid)
        Assert.assertNotNull("Joined XObject is not null", xObject)
        Assert.assertEquals("Statement is associated with expected contentEntryUid",
                contentEntryUid, xObject!!.objectContentEntryUid)
        Assert.assertEquals("ContentEntry itself has contentEntryUid set", contentEntryUid,
                statement?.statementContentEntryUid)
    }


    @Test
    @Throws(IOException::class)
    fun givenAValidStatement_whenPostRequestHasQueryParamsWithMethodisPut_thenShouldReturn204() {

        val urlString = "http://localhost:" + httpd.listeningPort + "/xapi/1234/statements?method=PUT"

        val tmpFile = File.createTempFile("testStatement", "statement")
        extractTestResourceToFile("/com/ustadmobile/port/sharedse/fullstatement", tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        val httpCon = URL(urlString).openConnection() as HttpURLConnection
        httpCon.doOutput = true
        httpCon.requestMethod = "POST"
        val out = OutputStreamWriter(
                httpCon.outputStream)
        out.write(content)
        out.close()
        httpCon.connect()

        val code = httpCon.responseCode

        Assert.assertEquals(204, code.toLong())
        val statement = appRepo!!.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement!!.statementId)

    }

    @Test
    @Throws(IOException::class)
    fun givenAValidStatement_whenPutRequestHasStatementIdParam_thenShouldReturn() {

        val urlString = "http://localhost:" + httpd.listeningPort + "/xapi/1234/statements?statementId=" +
                URLEncoder.encode("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", StandardCharsets.UTF_8.toString())

        val tmpFile = File.createTempFile("testStatement", "statement")
        extractTestResourceToFile("/com/ustadmobile/port/sharedse/fullstatement", tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))
        println(content)

        val httpCon = URL(urlString).openConnection() as HttpURLConnection
        httpCon.doOutput = true
        httpCon.requestMethod = "PUT"
        val out = OutputStreamWriter(
                httpCon.outputStream)
        out.write(content)
        out.close()
        httpCon.connect()

        val code = httpCon.responseCode

        Assert.assertEquals(204, code.toLong())
        val statement = appRepo!!.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement!!.statementId)
    }


}
