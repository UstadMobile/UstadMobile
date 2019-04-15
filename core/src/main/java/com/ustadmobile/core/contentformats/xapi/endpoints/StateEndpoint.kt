package com.ustadmobile.core.contentformats.xapi.endpoints

import com.google.gson.Gson
import com.ustadmobile.core.contentformats.xapi.Actor
import com.ustadmobile.core.contentformats.xapi.State
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.getAgent
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateState
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateStateContent
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.AgentDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.StateContentDao
import com.ustadmobile.core.db.dao.StateDao
import java.util.*

class StateEndpoint(db: UmAppDatabase, private val gson: Gson) {
    private val agentDao: AgentDao = db.agentDao
    private val stateDao: StateDao = db.stateDao
    private val stateContentDao: StateContentDao = db.stateContentDao
    private val personDao: PersonDao = db.personDao


    @Throws(IllegalArgumentException::class)
    fun storeState(state: State) {

        StatementEndpoint.checkValidActor(state.agent!!)

        val agentEntity = getAgent(agentDao, personDao, state.agent!!)

        val stateEntity = insertOrUpdateState(stateDao, state, agentEntity.agentUid)

        insertOrUpdateStateContent(stateContentDao, state.content!!, stateEntity)

    }


    fun getStateContent(stateId: String): String {

        val entity = stateDao.findByStateId(stateId)
        val list = stateContentDao.findAllStateContentWithStateUid(entity.stateUid)
        val contentMap = HashMap<String, String>()
        if (list != null) {
            for (contentEntity in list) {
                contentMap[contentEntity.stateContentKey] = contentEntity.stateContentValue
            }
        }

        return gson.toJson(contentMap)
    }

    fun deleteStateContent(stateId: String) {

        stateDao.setStateInActive(stateId, false)
    }

    fun getListOfStateId(agentJson: String, activityId: String, registration: String, since: String): String {

        val agent = gson.fromJson(agentJson, Actor::class.java)

        val agentEntity = getAgent(agentDao, personDao, agent)

        val list = stateDao.findStateIdByAgentAndActivity(agentEntity.agentUid, activityId, registration, since)

        val idList = ArrayList<String>()
        for (state in list) {
            idList.add(state.stateId)
        }

        return gson.toJson(idList)

    }

    fun deleteListOfStates(agentJson: String, activityId: String, registration: String, since: String) {

        val agent = gson.fromJson(agentJson, Actor::class.java)

        val agentEntity = getAgent(agentDao, personDao, agent)

        stateDao.updateStateToInActive(agentEntity.agentUid, activityId, registration, since, false)
    }
}
