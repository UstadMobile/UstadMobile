package com.ustadmobile.port.sharedse.contentformats.xapi.endpoints

import com.google.gson.Gson
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.AgentDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.StateContentDao
import com.ustadmobile.core.db.dao.StateDao
import com.ustadmobile.core.contentformats.xapi.Actor
import com.ustadmobile.core.contentformats.xapi.State
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStateEndpoint
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.deleteAndInsertNewStateContent
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.getAgent
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateState
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateStateContent
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import java.util.*
import com.ustadmobile.door.ext.DoorTag

class XapiStateEndpointImpl(val endpoint: Endpoint, override val di: DI) : XapiStateEndpoint {

    private val db: UmAppDatabase by on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val repo: UmAppDatabase by on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val gson: Gson by di.instance()


    @Throws(StatementRequestException::class)
    override fun storeState(state: State) {
        val agentVal = state.agent ?: throw IllegalArgumentException("State has null agent")
        val contentVal = state.content ?: throw IllegalArgumentException("State has null content")

        XapiStatementEndpointImpl.checkValidActor(agentVal)

        val agentEntity = getAgent(repo.agentDao, repo.personDao, agentVal)

        val stateEntity = insertOrUpdateState(repo.stateDao, state, agentEntity.agentUid)

        insertOrUpdateStateContent(repo.stateContentDao, contentVal, stateEntity)

    }

    override fun overrideState(state: State) {

        XapiStatementEndpointImpl.checkValidActor(state.agent!!)

        val agentEntity = getAgent(repo.agentDao, repo.personDao, state.agent!!)

        val stateEntity = insertOrUpdateState(repo.stateDao, state, agentEntity.agentUid)

        deleteAndInsertNewStateContent(repo.stateContentDao, state.content!!, stateEntity)

    }

    override fun getContent(stateId: String, agentJson: String, activityId: String, registration: String, since: String): String {
        return if (stateId.isEmpty()) {
            getListOfStateId(agentJson, activityId, registration, since)
        } else {
            getStateContent(stateId, agentJson, activityId, registration)
        }
    }


    fun getStateContent(stateId: String, agentJson: String, activityId: String, registration: String): String {

        val agent = gson.fromJson(agentJson, Actor::class.java)

        val agentEntity = getAgent(repo.agentDao, repo.personDao, agent)

        val entity = db.stateDao.findByStateId(stateId, agentEntity.agentUid, activityId, registration)
        val list = db.stateContentDao.findAllStateContentWithStateUid(entity!!.stateUid)
        val contentMap = HashMap<String?, String?>()
        for (contentEntity in list) {
            contentMap[contentEntity.stateContentKey] = contentEntity.stateContentValue
        }

        return gson.toJson(contentMap)
    }

    fun getListOfStateId(agentJson: String, activityId: String, registration: String, since: String): String {

        val agent = gson.fromJson(agentJson, Actor::class.java)

        val agentEntity = getAgent(db.agentDao, db.personDao, agent)

        val list = db.stateDao.findStateIdByAgentAndActivity(agentEntity.agentUid, activityId, registration, since)

        val idList = ArrayList<String?>()
        for (state in list) {
            idList.add(state.stateId)
        }

        return gson.toJson(idList)

    }

    override fun deleteStateContent(stateId: String, agentJson: String, activityId: String, registration: String) {

        val agent = gson.fromJson(agentJson, Actor::class.java)

        val agentEntity = getAgent(db.agentDao, db.personDao, agent)

        repo.stateDao.setStateInActive(stateId, agentEntity.agentUid, activityId, registration,
            false, getSystemTimeInMillis())
    }


    override fun deleteListOfStates(agentJson: String, activityId: String, registration: String) {

        val agent = gson.fromJson(agentJson, Actor::class.java)

        val agentEntity = getAgent(db.agentDao, db.personDao, agent)

        repo.stateDao.updateStateToInActive(agentEntity.agentUid, activityId, registration,
            false, getSystemTimeInMillis())
    }

}
