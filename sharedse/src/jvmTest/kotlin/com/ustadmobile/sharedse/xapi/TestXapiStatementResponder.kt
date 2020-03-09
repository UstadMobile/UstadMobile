package com.ustadmobile.sharedse.xapi

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.port.sharedse.impl.http.XapiStatementResponder
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import fi.iki.elonen.router.RouterNanoHTTPD
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class TestXapiStatementResponder {

    internal lateinit var httpd: RouterNanoHTTPD
    private var appRepo: UmAppDatabase? = null

    val context = Any()

    @Before
    @Throws(IOException::class)
    fun setup() {
        checkJndiSetup()
        val appDatabase = UmAppDatabase.Companion.getInstance(context)
        appDatabase.clearAllTables()
        appDatabase.preload()
        appRepo = appDatabase

        httpd = RouterNanoHTTPD(0)
        httpd.addRoute("/xapi/:contentEntryUid/statements", XapiStatementResponder::class.java, appRepo)
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
