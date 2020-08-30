package com.ustadmobile.sharedse.xapi

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.contentformats.xapi.Actor
import com.ustadmobile.core.contentformats.xapi.ContextActivity
import com.ustadmobile.core.contentformats.xapi.State
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStateEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.sharedse.contentformats.xapi.ContextDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementSerializer
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStateEndpointImpl
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.kodein.di.*
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.naming.InitialContext

class TestStateEndpoint {

    private lateinit var repo: UmAppDatabase

    private var contentMapToken = object : TypeToken<HashMap<String, Any>>() {

    }.type

    private lateinit var gson: Gson

    private lateinit var endpoint: XapiStateEndpoint

    private lateinit var di: DI

    val context = Any()

    @Before
    fun setup() {
        checkJndiSetup()
        val endpointScope = EndpointScope()
        val endpointUrl = Endpoint("http://localhost:8087/")
        di = DI {
            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(endpointScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                spy(UmAppDatabase.getInstance(Any(), dbName).also {
                    it.clearAllTables()
                    it.preload()
                })
            }
            bind<Gson>() with singleton {
                val builder = GsonBuilder()
                builder.registerTypeAdapter(Statement::class.java, StatementSerializer())
                builder.registerTypeAdapter(Statement::class.java, StatementDeserializer())
                builder.registerTypeAdapter(ContextActivity::class.java, ContextDeserializer())
                builder.create()
            }
            bind<XapiStateEndpoint>() with singleton {
                XapiStateEndpointImpl(endpointUrl, di)
            }
        }

        gson = di.direct.instance()
        repo = di.on(endpointUrl).direct.instance(tag = UmAppDatabase.TAG_DB)
        endpoint = di.on(endpointUrl).direct.instance()

    }


    @Test
    @Throws(IOException::class)
    fun givenStateObject_checkExistsInDb() {

        val activityId = "http://www.example.com/activities/1"
        val agentJson = "{\"objectType\": \"Agent\", \"name\": \"John Smith\", \"account\":{\"name\": \"123\", \"homePage\": \"http://www.example.com/users/\"}}"
        val stateId = "http://www.example.com/states/1"

        val tmpFile = File.createTempFile("testStateEndpoint", "stateEndpoint")
        extractTestResourceToFile(state, tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        val gson = Gson()
        val agent = gson.fromJson(agentJson, Actor::class.java)
        val contentMap = gson.fromJson<HashMap<String, Any>>(content, contentMapToken)

        val state = State(stateId, agent, activityId, contentMap, "")
        endpoint.storeState(state)

        val agentEntity = XapiUtil.getAgent(repo!!.agentDao, repo!!.personDao, state.agent!!)

        val stateEntity = repo!!.stateDao.findByStateId("http://www.example.com/states/1", agentEntity.agentUid, activityId, state.registration)


        Assert.assertEquals("matches activity id", state.activityId, stateEntity?.activityId)
        Assert.assertEquals("matches actor", state.agent!!.account!!.name, agentEntity.agentAccountName)

        val contentEntityWebsite = repo!!.stateContentDao.findStateContentByKeyAndStateUid("website", stateEntity!!.stateUid)

        Assert.assertEquals("matches value",
                "{name=Parthenon, icon=Part}", contentEntityWebsite?.stateContentValue)

        val contentEntityVisit = repo!!.stateContentDao.findStateContentByKeyAndStateUid("visited", stateEntity.stateUid)

        Assert.assertEquals("matches value",
                "false", contentEntityVisit?.stateContentValue)

        val contentEntityVisitRange = repo!!.stateContentDao.findStateContentByKeyAndStateUid("visitrange", stateEntity.stateUid)

        Assert.assertEquals("matches value",
                ".25", contentEntityVisitRange?.stateContentValue)


    }

    companion object {

        private const val state = "/com/ustadmobile/port/sharedse/xapi/state"
    }

}
