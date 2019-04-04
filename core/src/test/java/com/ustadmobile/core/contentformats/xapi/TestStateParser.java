package com.ustadmobile.core.contentformats.xapi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ustadmobile.core.contentformats.xapi.endpoints.StateEndpoint;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.AgentEntity;
import com.ustadmobile.lib.db.entities.StateContentEntity;
import com.ustadmobile.lib.db.entities.StateEntity;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class TestStateParser {

    private Gson gson;
    private UmAppDatabase repo;

    private static final String state = "/com/ustadmobile/core/contentformats/xapi/state";

    Type contentMapToken = new TypeToken<HashMap<String, Object>>() {
    }.getType();

    @Before
    public void setup() {

        UmAppDatabase db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        db.clearAllTables();
        repo = db.getRepository("http://localhost/dummy/", "");

        gson = new Gson();

    }


    @Test
    public void givenStateObject_checkExistsInDb() throws IOException {

        String activityId = "http://www.example.com/activities/1";
        String agentJson = "{\"objectType\": \"Agent\", \"name\": \"John Smith\", \"account\":{\"name\": \"123\", \"homePage\": \"http://www.example.com/users/\"}}";
        String stateId = "http://www.example.com/states/1";

        String content = UMIOUtils.readStreamToString(getClass().getResourceAsStream(state));

        Gson gson = new Gson();
        Actor agent = gson.fromJson(agentJson, Actor.class);
        HashMap<String, Object> contentMap = gson.fromJson(content, contentMapToken);

        State state = new State(stateId, agent, activityId, contentMap);
        StateEndpoint endpoint = new StateEndpoint(repo, gson);
        endpoint.storeState(state);

        StateEntity stateEntity = repo.getStateDao().findByStateId("http://www.example.com/states/1");
        AgentEntity agentEntity = repo.getAgentDao().findByUid(stateEntity.getAgentUid());

        Assert.assertEquals("matches activity id", state.getActivityId(), stateEntity.getActivityId());
        Assert.assertEquals("matches actor", state.getAgent().getAccount().getName(), agentEntity.getAgentAccountName());

        StateContentEntity contentEntityWebsite = repo.getStateContentDao().findStateContentByKeyAndStateUid("website", stateEntity.getStateUid());

        Assert.assertEquals("matches value",
                "{name=Parthenon, icon=Part}", contentEntityWebsite.getStateContentValue());

        StateContentEntity contentEntityVisit = repo.getStateContentDao().findStateContentByKeyAndStateUid("visited", stateEntity.getStateUid());

        Assert.assertEquals("matches value",
                "false", contentEntityVisit.getStateContentValue());

        StateContentEntity contentEntityVisitRange = repo.getStateContentDao().findStateContentByKeyAndStateUid("visitrange", stateEntity.getStateUid());

        Assert.assertEquals("matches value",
                ".25", contentEntityVisitRange.getStateContentValue());


    }

}
