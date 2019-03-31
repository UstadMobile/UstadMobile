package com.ustadmobile.core.contentformats.xapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.core.contentformats.xapi.endpoints.StatementEndpoint;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.AgentDao;
import com.ustadmobile.core.db.dao.ContextXObjectStatementJoinDao;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.lib.db.entities.AgentEntity;
import com.ustadmobile.lib.db.entities.ContextXObjectStatementJoin;
import com.ustadmobile.lib.db.entities.StatementEntity;
import com.ustadmobile.lib.db.entities.VerbEntity;
import com.ustadmobile.lib.db.entities.XObjectEntity;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestStatementParser {

    public final String contextWithObject = "/com/ustadmobile/core/contentformats/xapi/contextWitObject";
    public final String fullstatement = "/com/ustadmobile/core/contentformats/xapi/fullstatement";
    public final String simpleStatement = "/com/ustadmobile/core/contentformats/xapi/simpleStatment";
    public final String subStatement = "/com/ustadmobile/core/contentformats/xapi/substatement";
    private UmAppDatabase repo;
    private Gson gson;

    @Before
    public void setup() {

        UmAppDatabase db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        db.clearAllTables();
        repo = db.getRepository("http://localhost/dummy/", "");

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Statement.class, new StatementSerializer());
        builder.registerTypeAdapter(Statement.class, new StatementDeserializer());
        gson = builder.create();

    }

    @Test
    public void givenValidStatement_parseAll() throws IOException {

        Statement statement = gson.fromJson((UMIOUtils.readStreamToString(getClass().getResourceAsStream(simpleStatement))), Statement.class);
        StatementEndpoint endpoint = new StatementEndpoint(repo, gson);
        List<Statement> list = new ArrayList<>();
        list.add(statement);
        endpoint.storeStatements(list);

        StatementEntity entity = repo.getStatementDao().findByStatementId("fd41c918-b88b-4b20-a0a5-a4c32391aaa0");
        AgentEntity agent = repo.getAgentDao().getAgentByAnyId("", "mailto:user@example.com", "", "");
        VerbEntity verb = repo.getVerbDao().findByUrl("http://example.com/xapi/verbs#sent-a-statement");
        XObjectEntity xobject = repo.getXObjectDao().findByObjectId("http://example.com/xapi/activity/simplestatement");

        Assert.assertEquals("joined to agent", entity.getAgentUid(), agent.getAgentUid());
        Assert.assertEquals("mailto:user@example.com", agent.getAgentMbox());

        Assert.assertEquals("joined to verb", entity.getVerbUid(), verb.getVerbUid());
        Assert.assertEquals("joined to object", entity.getXObjectUid(), xobject.getXObjectUid());

    }

    @Test
    public void givenValidStatement_parseContext() throws IOException {

        Statement statement = gson.fromJson(UMIOUtils.readStreamToString(getClass().getResourceAsStream(contextWithObject)), Statement.class);
        StatementEndpoint endpoint = new StatementEndpoint(repo, gson);
        List<Statement> list = new ArrayList<>();
        list.add(statement);
        endpoint.storeStatements(list);

        StatementEntity entity = repo.getStatementDao().findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee");
        AgentEntity agent = repo.getAgentDao().getAgentByAnyId("", "mailto:sally@example.com", "", "");
        VerbEntity verb = repo.getVerbDao().findByUrl("http://adlnet.gov/expapi/verbs/experienced");
        XObjectEntity xobject = repo.getXObjectDao().findByObjectId("http://example.com/activities/solo-hang-gliding");
        XObjectEntity parent = repo.getXObjectDao().findByObjectId("http://example.com/activities/hang-gliding-class-a");
        ContextXObjectStatementJoin contextJoin = repo.getContextXObjectStatementJoinDao()
                .findByStatementAndObjectUid(entity.getStatementUid(), parent.getXObjectUid());

        Assert.assertEquals("joined to agent", entity.getAgentUid(), agent.getAgentUid());
        Assert.assertEquals("mailto:sally@example.com", agent.getAgentMbox());

        Assert.assertEquals("joined to verb", entity.getVerbUid(), verb.getVerbUid());
        Assert.assertEquals("joined to object", entity.getXObjectUid(), xobject.getXObjectUid());

        Assert.assertEquals("context statement joined with parent flag", contextJoin.getContextActivityFlag(), ContextXObjectStatementJoinDao.CONTEXT_FLAG_PARENT);
        Assert.assertEquals("context statement joined matches with objectuid", parent.getXObjectUid(), contextJoin.getContextXObjectUid());
        Assert.assertEquals("context statement joined matches with statement", entity.getStatementUid(), contextJoin.getContextStatementUid());


    }

    @Test
    public void givenValidStatement_parseFull() throws IOException {

        Statement statement = gson.fromJson(UMIOUtils.readStreamToString(getClass().getResourceAsStream(fullstatement)), Statement.class);
        StatementEndpoint endpoint = new StatementEndpoint(repo, gson);
        List<Statement> list = new ArrayList<>();
        list.add(statement);
        endpoint.storeStatements(list);

        StatementEntity entity = repo.getStatementDao().findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee");
        AgentEntity agent = repo.getAgentDao().getAgentByAnyId("", "mailto:teampb@example.com", "", "");
        VerbEntity verb = repo.getVerbDao().findByUrl("http://adlnet.gov/expapi/verbs/attended");
        XObjectEntity xobject = repo.getXObjectDao().findByObjectId("http://www.example.com/meetings/occurances/34534");
        AgentEntity instructor = repo.getAgentDao().getAgentByAnyId("", "", "13936749", "");
        AgentEntity authority = repo.getAgentDao().getAgentByAnyId("", "", "anonymous", "");
        AgentEntity team = repo.getAgentDao().getAgentByAnyId("", "mailto:teampb@example.com", "", "");
        XObjectEntity parent = repo.getXObjectDao().findByObjectId("http://www.example.com/meetings/series/267");
        ContextXObjectStatementJoin contextJoin = repo.getContextXObjectStatementJoinDao()
                .findByStatementAndObjectUid(entity.getStatementUid(), parent.getXObjectUid());

        Assert.assertEquals("joined to agent", entity.getAgentUid(), agent.getAgentUid());
        Assert.assertEquals("mailto:teampb@example.com", agent.getAgentMbox());

        Assert.assertEquals("joined to verb", entity.getVerbUid(), verb.getVerbUid());
        Assert.assertEquals("joined to object", entity.getXObjectUid(), xobject.getXObjectUid());

        Assert.assertEquals("context registration matched", "ec531277-b57b-4c15-8d91-d292c5b2b8f7", entity.getContextRegistration());
        Assert.assertEquals("context platform matched", "Example virtual meeting software", entity.getContextPlatform());
        Assert.assertEquals("context statement matched", "6690e6c9-3ef0-4ed3-8b37-7f3964730bee", entity.getContextStatementId());

        Assert.assertEquals("joined to instructor", entity.getInstructorUid(), instructor.getAgentUid());
        Assert.assertEquals("13936749", instructor.getAgentAccountName());

        Assert.assertEquals("joined to authority", entity.getAuthorityUid(), authority.getAgentUid());
        Assert.assertEquals("joined to team", entity.getTeamUid(), team.getAgentUid());

        Assert.assertEquals("context statement joined with parent flag", contextJoin.getContextActivityFlag(), ContextXObjectStatementJoinDao.CONTEXT_FLAG_PARENT);
        Assert.assertEquals("context statement joined matches with objectuid", parent.getXObjectUid(), contextJoin.getContextXObjectUid());
        Assert.assertEquals("context statement joined matches with statement", entity.getStatementUid(), contextJoin.getContextStatementUid());

        Assert.assertTrue("result success matched", entity.isResultSuccess());
        Assert.assertTrue("result completion matched", entity.isResultCompletion());
        Assert.assertEquals("result response matched", "We agreed on some example actions.", entity.getResultResponse());
        Assert.assertEquals("result duration matched", UMTinCanUtil.parse8601Duration("PT1H0M0S"), entity.getResultDuration());

    }

    @Test
    public void givenValidStatement_parseSub() throws IOException {

        Statement statement = gson.fromJson(UMIOUtils.readStreamToString(getClass().getResourceAsStream(subStatement)), Statement.class);
        StatementEndpoint endpoint = new StatementEndpoint(repo, gson);
        List<Statement> list = new ArrayList<>();
        list.add(statement);
        endpoint.storeStatements(list);

        StatementEntity entity = repo.getStatementDao().findByStatementId("fd41c918-b88b-4b20-a0a5-a4c32391aaa0");
        AgentEntity agent = repo.getAgentDao().getAgentByAnyId("", "mailto:test@example.com", "", "");
        VerbEntity verb = repo.getVerbDao().findByUrl("http://example.com/planned");
        AgentEntity subActor = repo.getAgentDao().getAgentByAnyId("", "mailto:test@example.com", "", "");
        VerbEntity subVerb = repo.getVerbDao().findByUrl("http://example.com/visited");
        XObjectEntity subobject = repo.getXObjectDao().findByObjectId("http://example.com/website");

        Assert.assertEquals("joined to agent", entity.getAgentUid(), agent.getAgentUid());
        Assert.assertEquals("mailto:test@example.com", agent.getAgentMbox());

        Assert.assertEquals("joined to verb", entity.getVerbUid(), verb.getVerbUid());

        Assert.assertEquals("joined to substatement actor", entity.getSubStatementActorUid(), subActor.getAgentUid());
        Assert.assertEquals("mailto:test@example.com", subActor.getAgentMbox());

        Assert.assertEquals("joined to substatement verb", entity.getSubstatementVerbUid(), subVerb.getVerbUid());
        Assert.assertEquals("joined to substatement object", entity.getSubStatementObjectUid(), subobject.getXObjectUid());
        Assert.assertEquals("with substatment, object should be null", 0, entity.getXObjectUid());


    }

    @Test
    public void givenAgentEntity_daoReturnsTheCorrectAgent() {
        AgentDao agentDao = repo.getAgentDao();
        AgentEntity agentEntity = new AgentEntity();
        agentEntity.setAgentMbox("samih@ustadmobile.com");
        agentEntity.setAgentOpenid(null);
        agentEntity.setAgentUid(agentDao.insert(agentEntity));

        AgentEntity secondAgent = new AgentEntity();
        secondAgent.setAgentMbox(null);
        secondAgent.setAgentOpenid("mike@ustadmobile.com");
        secondAgent.setAgentUid(agentDao.insert(secondAgent));

        AgentEntity thirdAgent = new AgentEntity();
        thirdAgent.setAgentMbox("sd@ustad.com");
        thirdAgent.setAgentOpenid(null);
        thirdAgent.setAgentUid(agentDao.insert(thirdAgent));

        AgentEntity entity = agentDao.getAgentByAnyId(
                agentEntity.getAgentOpenid(),
                agentEntity.getAgentMbox(),
                agentEntity.getAgentAccountName(),
                agentEntity.getAgentMbox_sha1sum());

        Assert.assertEquals("not same mbox", agentEntity.getAgentMbox(), entity.getAgentMbox());
    }


}
