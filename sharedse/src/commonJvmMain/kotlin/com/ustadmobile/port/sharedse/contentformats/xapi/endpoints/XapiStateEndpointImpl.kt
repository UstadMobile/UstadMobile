package com.ustadmobile.port.sharedse.contentformats.xapi.endpoints

import com.google.gson.Gson
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.AgentDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.StateContentDao
import com.ustadmobile.core.db.dao.StateDao
import com.ustadmobile.port.sharedse.contentformats.xapi.Actor
import com.ustadmobile.port.sharedse.contentformats.xapi.State
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.deleteAndInsertNewStateContent
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.getAgent
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateState
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateStateContent
import java.util.*

class StateEndpoint(db: UmAppDatabase, private val gson: Gson, private val contentType: String?) {
    private val agentDao: AgentDao = db.agentDao
    private val stateDao: StateDao = db.stateDao
    private val stateContentDao: StateContentDao = db.stateContentDao
    private val personDao: PersonDao = db.personDao


    @Throws(StatementRequestException::class)
    fun storeState(state: State) {

        isContentTypeJson()

        StatementEndpoint.checkValidActor(state.agent!!)

        val agentEntity = getAgent(agentDao, personDao, state.agent!!)

        val stateEntity = insertOrUpdateState(stateDao, state, agentEntity.agentUid)

        insertOrUpdateStateContent(stateContentDao, state.content!!, stateEntity)

    }

    fun overrideState(state: State) {

        isContentTypeJson()

        StatementEndpoint.checkValidActor(state.agent!!)

        val agentEntity = getAgent(agentDao, personDao, state.agent!!)

        val stateEntity = insertOrUpdateState(stateDao, state, agentEntity.agentUid)

        deleteAndInsertNewStateContent(stateContentDao, state.content!!, stateEntity)

    }

    fun getContent(stateId: String, agentJson: String, activityId: String, registration: String, since: String): String {
        return if (stateId.isEmpty()) {
            getListOfStateId(agentJson, activityId, registration, since)
        } else {
            getStateContent(stateId, agentJson, activityId, registration)
        }
    }


    fun getStateContent(stateId: String, agentJson: String, activityId: String, registration: String): String {

        val agent = gson.fromJson(agentJson, Actor::class.java)

        val agentEntity = getAgent(agentDao, personDao, agent)

        val entity = stateDao.findByStateId(stateId, agentEntity.agentUid, activityId, registration)
        val list = stateContentDao.findAllStateContentWithStateUid(entity!!.stateUid)
        val contentMap = HashMap<String?, String?>()
        for (contentEntity in list) {
            contentMap[contentEntity.stateContentKey] = contentEntity.stateContentValue
        }

        return gson.toJson(contentMap)
    }

    fun getListOfStateId(agentJson: String, activityId: String, registration: String, since: String): String {

        val agent = gson.fromJson(agentJson, Actor::class.java)

        val agentEntity = getAgent(agentDao, personDao, agent)

        val list = stateDao.findStateIdByAgentAndActivity(agentEntity.agentUid, activityId, registration, since)

        val idList = ArrayList<String?>()
        for (state in list) {
            idList.add(state.stateId)
        }

        return gson.toJson(idList)

    }

    fun deleteStateContent(stateId: String, agentJson: String, activityId: String, registration: String) {

        val agent = gson.fromJson(agentJson, Actor::class.java)

        val agentEntity = getAgent(agentDao, personDao, agent)

        stateDao.setStateInActive(stateId, agentEntity.agentUid, activityId, registration, false)
    }


    fun deleteListOfStates(agentJson: String, activityId: String, registration: String) {

        val agent = gson.fromJson(agentJson, Actor::class.java)

        val agentEntity = getAgent(agentDao, personDao, agent)

        stateDao.updateStateToInActive(agentEntity.agentUid, activityId, registration, false)
    }

    private fun isContentTypeJson() {
        if (contentType?.isEmpty() == true || contentType != "application/json") {
            throw StatementRequestException("Content Type missing or not set to application/json")
        }
    }

}
