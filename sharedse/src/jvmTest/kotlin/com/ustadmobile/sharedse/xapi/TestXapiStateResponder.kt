package com.ustadmobile.sharedse.xapi

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.port.sharedse.impl.http.XapiStateResponder
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
import java.util.*

class TestXapiStateResponder {

    private var appRepo: UmAppDatabase? = null
    private var httpd: RouterNanoHTTPD? = null

    val context = Any()

    internal var contentMapToken = object : TypeToken<HashMap<String, String>>() {

    }.type

    @Before
    @Throws(IOException::class)
    fun setup() {
        checkJndiSetup()
        val appDatabase = UmAppDatabase.Companion.getInstance(context)
        appDatabase.clearAllTables()
        appRepo = appDatabase //appDatabase.getRepository("http://localhost/dummy/", "")

        httpd = RouterNanoHTTPD(0)
        httpd!!.addRoute("/xapi/activities/state(.*)+", XapiStateResponder::class.java, appRepo)
        httpd!!.start()
    }

    @Test
    @Throws(IOException::class)
    fun testput() {

        var urlString = "http://localhost:" + httpd!!.listeningPort + "/xapi/activities/state"

        val tmpFile = File.createTempFile("testState", "state")
        extractTestResourceToFile("/com/ustadmobile/port/sharedse/state", tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        urlString += "?activityId=" +
                URLEncoder.encode("http://www.example.com/activities/1", StandardCharsets.UTF_8.toString()) +
                "&agent=" +
                URLEncoder.encode("{\"objectType\": \"Agent\", \"name\": \"John Smith\", \"account\":{\"name\": \"123\", \"homePage\": \"http://www.example.com/users/\"}}",
                        StandardCharsets.UTF_8.toString()) +
                "&stateId=" +
                URLEncoder.encode("http://www.example.com/states/1", StandardCharsets.UTF_8.toString())
        val httpCon = URL(urlString).openConnection() as HttpURLConnection
        httpCon.doOutput = true
        httpCon.requestMethod = "PUT"
        httpCon.setRequestProperty("Content-Type", "application/json")
        val out = OutputStreamWriter(
                httpCon.outputStream)
        out.write(content)
        out.close()

        httpCon.connect()
        val code = httpCon.responseCode

        Assert.assertEquals(204, code.toLong())
        val agentEntity = appRepo!!.agentDao.getAgentByAnyId("", "", "123", "http://www.example.com/users/", "")
        val stateEntity = appRepo!!.stateDao.findByStateId("http://www.example.com/states/1", agentEntity!!.agentUid, "http://www.example.com/activities/1", "")
        Assert.assertEquals("http://www.example.com/activities/1", stateEntity!!.activityId)
    }

    @Test
    @Throws(IOException::class)
    fun testPost() {

        var urlString = "http://localhost:" + httpd!!.listeningPort + "/xapi/activities/state"
        urlString += "?activityId=" +
                URLEncoder.encode("http://www.example.com/activities/1", StandardCharsets.UTF_8.toString()) +
                "&agent=" +
                URLEncoder.encode("{\"objectType\": \"Agent\", \"name\": \"John Smith\", \"account\":{\"name\": \"123\", \"homePage\": \"http://www.example.com/users/\"}}",
                        StandardCharsets.UTF_8.toString()) +
                "&stateId=" +
                URLEncoder.encode("http://www.example.com/states/1", StandardCharsets.UTF_8.toString())

        val code = PostMethod(urlString).responseCode

        Assert.assertEquals(204, code.toLong())
        val agentEntity = appRepo!!.agentDao.getAgentByAnyId("", "", "123", "http://www.example.com/users/", "")
        val stateEntity = appRepo!!.stateDao.findByStateId("http://www.example.com/states/1", agentEntity!!.agentUid, "http://www.example.com/activities/1", "")
        Assert.assertEquals("http://www.example.com/activities/1", stateEntity!!.activityId)
    }

    @ExperimentalStdlibApi
    @Test
    @Throws(IOException::class)
    fun testAll() {
        var urlString = "http://localhost:" + httpd!!.listeningPort + "/xapi/activities/state"
        urlString += "?activityId=" +
                URLEncoder.encode("http://www.example.com/activities/1", StandardCharsets.UTF_8.toString()) +
                "&agent=" +
                URLEncoder.encode("{\"objectType\": \"Agent\", \"name\": \"John Smith\", \"account\":{\"name\": \"123\", \"homePage\": \"http://www.example.com/users/\"}}",
                        StandardCharsets.UTF_8.toString()) +
                "&stateId=" +
                URLEncoder.encode("http://www.example.com/states/1", StandardCharsets.UTF_8.toString())

        val code = PostMethod(urlString).responseCode

        Assert.assertEquals(204, code.toLong())
        val agentEntity = appRepo!!.agentDao.getAgentByAnyId("", "", "123", "http://www.example.com/users/", "")
        val stateEntity = appRepo!!.stateDao.findByStateId("http://www.example.com/states/1", agentEntity!!.agentUid, "http://www.example.com/activities/1", "")
        Assert.assertEquals("http://www.example.com/activities/1", stateEntity!!.activityId)

        val getCon = URL(urlString).openConnection() as HttpURLConnection
        getCon.requestMethod = "GET"
        getCon.connect()

        val json = UMIOUtils.readStreamToString(getCon.inputStream)
        val contentMap = Gson().fromJson<HashMap<String, String>>(json, contentMapToken)
        Assert.assertEquals("Content matches", "Parthenon", contentMap["name"])

        val deleteCon = URL(urlString).openConnection() as HttpURLConnection
        deleteCon.requestMethod = "DELETE"
        deleteCon.connect()

        val deleteCode = deleteCon.responseCode

        Assert.assertEquals(204, deleteCode.toLong())

        val deletedState = appRepo!!.stateDao.findByStateId("http://www.example.com/states/1", agentEntity.agentUid, "http://www.example.com/activities/1", "")
        Assert.assertNull(deletedState)

    }

    private fun PostMethod(urlString: String): HttpURLConnection {
        val tmpFile = File.createTempFile("testState", "state")
        extractTestResourceToFile("/com/ustadmobile/port/sharedse/state", tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        val httpCon = URL(urlString).openConnection() as HttpURLConnection
        httpCon.doOutput = true
        httpCon.requestMethod = "POST"
        httpCon.setRequestProperty("Content-Type", "application/json")
        val out = OutputStreamWriter(
                httpCon.outputStream)
        out.write(content)
        out.close()
        httpCon.connect()

        return httpCon
    }

}
