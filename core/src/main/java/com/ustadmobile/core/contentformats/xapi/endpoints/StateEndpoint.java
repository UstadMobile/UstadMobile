package com.ustadmobile.core.contentformats.xapi.endpoints;

import com.google.gson.Gson;
import com.ustadmobile.core.contentformats.xapi.Actor;
import com.ustadmobile.core.contentformats.xapi.State;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.AgentDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.StateContentDao;
import com.ustadmobile.core.db.dao.StateDao;
import com.ustadmobile.lib.db.entities.AgentEntity;
import com.ustadmobile.lib.db.entities.StateContentEntity;
import com.ustadmobile.lib.db.entities.StateEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.getAgent;
import static com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateState;
import static com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateStateContent;

public class StateEndpoint {

    private final UmAppDatabase db;
    private final Gson gson;
    private final AgentDao agentDao;
    private final StateDao stateDao;
    private final StateContentDao stateContentDao;
    private final PersonDao personDao;

    public StateEndpoint(UmAppDatabase db, Gson gson) {
        this.db = db;
        this.gson = gson;

        personDao = db.getPersonDao();
        agentDao = db.getAgentDao();
        stateDao = db.getStateDao();
        stateContentDao = db.getStateContentDao();
    }


    public void storeState(State state) throws IllegalArgumentException {

        StatementEndpoint.checkValidActor(state.getAgent());

        AgentEntity agentEntity = getAgent(agentDao, personDao, state.getAgent());

        StateEntity stateEntity = insertOrUpdateState(stateDao, state, agentEntity.getAgentUid());

        insertOrUpdateStateContent(stateContentDao, state.getContent(), stateEntity);


    }


    public String getStateContent(String stateId) {

        StateEntity entity = stateDao.findByStateId(stateId);
        List<StateContentEntity> list = stateContentDao.findAllStateContentWithStateUid(entity.getStateUid());
        HashMap<String, String> contentMap = new HashMap<>();
        if (list != null) {
            for (StateContentEntity contentEntity : list) {
                contentMap.put(contentEntity.getStateContentKey(), contentEntity.getStateContentValue());
            }
        }

        return gson.toJson(contentMap);
    }

    public void deleteStateContent(String stateId) {

        stateDao.setStateInActive(stateId, false);
    }

    public String getListOfStateId(String agentJson, String activityId, String registration, String since) {

        Actor agent = gson.fromJson(agentJson, Actor.class);

        AgentEntity agentEntity = getAgent(agentDao, personDao, agent);

        List<StateEntity> list = stateDao.findStateIdByAgentAndActivity(agentEntity.getAgentUid(), activityId, registration, since);

        List<String> idList = new ArrayList<>();
        for (StateEntity state : list) {
            idList.add(state.getStateId());
        }

        return gson.toJson(idList);

    }

    public void deleteListOfStates(String agentJson, String activityId, String registration, String since) {

        Actor agent = gson.fromJson(agentJson, Actor.class);

        AgentEntity agentEntity = getAgent(agentDao, personDao, agent);

        stateDao.updateStateToInActive(agentEntity.getAgentUid(), activityId, registration, since, false);
    }
}
