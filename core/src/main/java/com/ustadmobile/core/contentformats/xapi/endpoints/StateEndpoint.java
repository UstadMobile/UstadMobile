package com.ustadmobile.core.contentformats.xapi.endpoints;

import com.google.gson.Gson;
import com.ustadmobile.core.contentformats.xapi.State;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.AgentDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.StateContentDao;
import com.ustadmobile.core.db.dao.StateDao;
import com.ustadmobile.lib.db.entities.AgentEntity;
import com.ustadmobile.lib.db.entities.StateEntity;

import static com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.*;

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

        AgentEntity agentEntity = insertOrUpdateAgent(agentDao, personDao, state.getAgent());

        StateEntity stateEntity = insertOrUpdateState(stateDao, state, agentEntity.getAgentUid());

        insertOrUpdateStateContent(stateContentDao, state.getContent(), stateEntity);


    }


}
