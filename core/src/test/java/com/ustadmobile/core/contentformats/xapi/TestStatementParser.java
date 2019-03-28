package com.ustadmobile.core.contentformats.xapi;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.AgentDao;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.AgentEntity;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TestStatementParser {

    public final String contextWithObject = "/com/ustadmobile/core/contentformats/xapi/contextWitObject";
    public final String fullstatement = "/com/ustadmobile/core/contentformats/xapi/fullstatement";
    public final String simpleStatement = "/com/ustadmobile/core/contentformats/xapi/simpleStatment";
    public final String subStatement = "/com/ustadmobile/core/contentformats/xapi/substatement";

    @Test
    public void givenValidStatement_parseAll() throws IOException {

        Statement statement = Statement.loadObject(UMIOUtils.readStreamToString(getClass().getResourceAsStream(simpleStatement)));

    }

    @Test
    public void givenValidStatement_parseContext() throws IOException {

        Statement statement = Statement.loadObject(UMIOUtils.readStreamToString(getClass().getResourceAsStream(contextWithObject)));

    }

    @Test
    public void givenValidStatement_parseFull() throws IOException {

        Statement statement = Statement.loadObject(UMIOUtils.readStreamToString(getClass().getResourceAsStream(fullstatement)));

    }

    @Test
    public void givenValidStatement_parseSub() throws IOException {

        Statement statement = Statement.loadObject(UMIOUtils.readStreamToString(getClass().getResourceAsStream(subStatement)));

    }

    @Test
    public void givenAgentEntity_daoReturnsTheCorrectAgent() {
        UmAppDatabase db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        UmAppDatabase repo = db.getRepository("http://localhost/dummy/", "");

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
