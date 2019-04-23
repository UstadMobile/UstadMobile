package com.ustadmobile.core.contentformats.xapi

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.contentformats.xapi.endpoints.StateEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.test.core.impl.PlatformTestUtil
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.*

class TestStateEndpoint {

    private var gson: Gson? = null
    private var repo: UmAppDatabase? = null

    private var contentMapToken = object : TypeToken<HashMap<String, Any>>() {

    }.type

    @Before
    fun setup() {

        val db = UmAppDatabase.getInstance(PlatformTestUtil.targetContext)
        db.clearAllTables()
        repo = db.getRepository("http://localhost/dummy/", "")

        gson = Gson()

    }


    @Test
    @Throws(IOException::class)
    fun givenStateObject_checkExistsInDb() {

        val activityId = "http://www.example.com/activities/1"
        val agentJson = "{\"objectType\": \"Agent\", \"name\": \"John Smith\", \"account\":{\"name\": \"123\", \"homePage\": \"http://www.example.com/users/\"}}"
        val stateId = "http://www.example.com/states/1"

        val content = UMIOUtils.readStreamToString(javaClass.getResourceAsStream(state))

        val gson = Gson()
        val agent = gson.fromJson(agentJson, Actor::class.java)
        val contentMap = gson.fromJson<HashMap<String, Any>>(content, contentMapToken)

        val state = State(stateId, agent, activityId, contentMap, "")
        val endpoint = StateEndpoint(repo!!, gson)
        endpoint.storeState(state)

        val agentEntity = XapiUtil.getAgent(repo!!.agentDao, repo!!.personDao, state.agent!!)

        val stateEntity = repo!!.stateDao.findByStateId("http://www.example.com/states/1", agentEntity.agentUid, activityId, state.registration)


        Assert.assertEquals("matches activity id", state.activityId, stateEntity.activityId)
        Assert.assertEquals("matches actor", state.agent!!.account!!.name, agentEntity.agentAccountName)

        val contentEntityWebsite = repo!!.stateContentDao.findStateContentByKeyAndStateUid("website", stateEntity.stateUid)

        Assert.assertEquals("matches value",
                "{name=Parthenon, icon=Part}", contentEntityWebsite.stateContentValue)

        val contentEntityVisit = repo!!.stateContentDao.findStateContentByKeyAndStateUid("visited", stateEntity.stateUid)

        Assert.assertEquals("matches value",
                "false", contentEntityVisit.stateContentValue)

        val contentEntityVisitRange = repo!!.stateContentDao.findStateContentByKeyAndStateUid("visitrange", stateEntity.stateUid)

        Assert.assertEquals("matches value",
                ".25", contentEntityVisitRange.stateContentValue)


    }

    companion object {

        private val state = "/com/ustadmobile/core/contentformats/xapi/state"
    }

}
