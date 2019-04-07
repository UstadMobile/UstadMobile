package com.ustadmobile.core.contentformats.xapi.endpoints;

import com.google.gson.Gson;
import com.ustadmobile.core.contentformats.xapi.Actor;
import com.ustadmobile.core.contentformats.xapi.Definition;
import com.ustadmobile.core.contentformats.xapi.State;
import com.ustadmobile.core.contentformats.xapi.Statement;
import com.ustadmobile.core.contentformats.xapi.XObject;
import com.ustadmobile.core.db.dao.AgentDao;
import com.ustadmobile.core.db.dao.ContextXObjectStatementJoinDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.StateContentDao;
import com.ustadmobile.core.db.dao.StateDao;
import com.ustadmobile.core.db.dao.StatementDao;
import com.ustadmobile.core.db.dao.VerbDao;
import com.ustadmobile.core.db.dao.XObjectDao;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.lib.db.entities.AgentEntity;
import com.ustadmobile.lib.db.entities.ContextXObjectStatementJoin;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.StateContentEntity;
import com.ustadmobile.lib.db.entities.StateEntity;
import com.ustadmobile.lib.db.entities.StatementEntity;
import com.ustadmobile.lib.db.entities.VerbEntity;
import com.ustadmobile.lib.db.entities.XObjectEntity;

import java.util.HashMap;

public class XapiUtil {

    public static AgentEntity getAgent(AgentDao dao, PersonDao personDao, Actor actor) {
        Person person = getPerson(personDao, actor);
        AgentEntity agentEntity = dao.getAgentByAnyId(
                actor.getOpenid(),
                actor.getMbox(),
                actor.getAccount() != null ? actor.getAccount().getName() : null,
                actor.getMbox_sha1sum());
        if (agentEntity == null) {
            agentEntity = new AgentEntity();
            agentEntity.setAgentOpenid(actor.getOpenid());
            agentEntity.setAgentMbox(actor.getMbox());
            agentEntity.setAgentAccountName(actor.getAccount() != null ? actor.getAccount().getName() : null);
            agentEntity.setAgentMbox_sha1sum(actor.getMbox_sha1sum());
            agentEntity.setAgentPersonUid(person != null ? person.getPersonUid() : 0);
            agentEntity.setAgentUid(dao.insert(agentEntity));
        }

        return agentEntity;
    }


    public static VerbEntity insertOrUpdateVerb(VerbDao dao, String urlId) {

        VerbEntity verbEntity = dao.findByUrl(urlId);
        if (verbEntity == null) {
            verbEntity = new VerbEntity();
            verbEntity.setUrlId(urlId);
            verbEntity.setVerbUid(dao.insert(verbEntity));
        }
        return verbEntity;

    }

    public static ContextXObjectStatementJoin insertOrUpdateContextStatementJoin(ContextXObjectStatementJoinDao dao, long statementUid, long objectUid, int flag) {

        ContextXObjectStatementJoin join = dao.findByStatementAndObjectUid(statementUid, objectUid);
        if (join == null) {
            join = new ContextXObjectStatementJoin();
            join.setContextActivityFlag(flag);
            join.setContextStatementUid(statementUid);
            join.setContextXObjectUid(objectUid);
            join.setContextXObjectStatementJoinUid(dao.insert(join));
        }
        return join;
    }

    public static Person getPerson(PersonDao dao, Actor actor) {
        Person person = null;
        if (actor.getAccount() != null) {
            person = dao.findByUsername(actor.getAccount().getName());
        }
        return person;
    }

    public static XObjectEntity insertOrUpdateXObject(XObjectDao dao, XObject xobject, Gson gson) {

        XObjectEntity entity = dao.findByObjectId(xobject.getId());
        if (entity == null) {
            entity = new XObjectEntity();
            entity.setObjectId(xobject.getId());
            entity.setObjectType(xobject.getObjectType());
            if (xobject.getDefinition() != null) {
                entity.setDefinitionType(xobject.getDefinition().getType());
                entity.setInteractionType(xobject.getDefinition().getInteractionType());
                entity.setCorrectResponsePattern(gson.toJson(xobject.getDefinition().getCorrectResponsePattern()));
            }
            entity.setXObjectUid(dao.insert(entity));
        } else {
            XObjectEntity xObjectEntity = new XObjectEntity();
            xObjectEntity.setObjectId(xobject.getId());
            xObjectEntity.setObjectType(xobject.getObjectType() != null ? xobject.getObjectType() : entity.getObjectType());
            if (xobject.getDefinition() != null) {
                Definition changedDefinition = xobject.getDefinition();
                xObjectEntity.setDefinitionType(changedDefinition.getType() != null && !changedDefinition.getType().isEmpty() ?
                        changedDefinition.getType() : entity.getDefinitionType());

                xObjectEntity.setInteractionType(changedDefinition.getInteractionType() != null && !changedDefinition.getInteractionType().isEmpty() ?
                        changedDefinition.getType() : entity.getInteractionType());

                xObjectEntity.setCorrectResponsePattern(changedDefinition.getCorrectResponsePattern() != null &&
                        changedDefinition.getCorrectResponsePattern().size() > 0 ?
                        gson.toJson(changedDefinition.getCorrectResponsePattern()) : entity.getCorrectResponsePattern());
            }

            if (!xObjectEntity.equals(entity)) {
                dao.update(xObjectEntity);
                entity = xObjectEntity;
            }
        }
        return entity;
    }

    public static StateEntity insertOrUpdateState(StateDao dao, State state, long agentUid) {
        StateEntity stateEntity = dao.findByStateId(state.getStateId());

        StateEntity changedState = new StateEntity(state.getActivityId(), agentUid,
                state.getRegistration(), state.getStateId(), true, System.currentTimeMillis());

        if (stateEntity == null) {
            changedState.setStateUid(dao.insert(changedState));
        } else {
            changedState.setStateUid(stateEntity.getStateUid());
            if (!changedState.equals(stateEntity)) {
                dao.update(changedState);
            }
        }
        return changedState;
    }

    public static void insertOrUpdateStateContent(StateContentDao dao, HashMap<String, Object> contentMap, StateEntity stateEntity) {

        for (String key : contentMap.keySet()) {

            Object value = contentMap.get(key);
            StateContentEntity content = dao.findStateContentByKeyAndStateUid(key, stateEntity.getStateUid());
            if (content == null) {
                StateContentEntity contentEntity = new StateContentEntity(key, stateEntity.getStateUid(), String.valueOf(value));
                contentEntity.setStateContentUid(dao.insert(contentEntity));
            } else {
                StateContentEntity changedContent = new StateContentEntity(key, stateEntity.getStateUid(), String.valueOf(value));
                changedContent.setStateContentUid(content.getStateContentUid());
                if (!changedContent.equals(content)) {
                    dao.update(changedContent);
                }
            }
        }
    }

    public static StatementEntity insertOrUpdateStatementEntity(StatementDao dao, Statement statement, Gson gson,
                                                                long personUid, long verbUid, long objectUid,
                                                                String contextStatementUid,
                                                                long instructorUid, long agentUid, long authorityUid, long teamUid,
                                                                long subActorUid, long subVerbUid, long subObjectUid) {

        StatementEntity statementEntity = dao.findByStatementId(statement.getId());
        if (statementEntity == null) {
            statementEntity = new StatementEntity();
            statementEntity.setPersonUid(personUid);
            statementEntity.setStatementId(statement.getId());
            statementEntity.setVerbUid(verbUid);
            statementEntity.setXObjectUid(objectUid);
            statementEntity.setAgentUid(agentUid);
            statementEntity.setAuthorityUid(authorityUid);
            statementEntity.setInstructorUid(instructorUid);
            statementEntity.setTeamUid(teamUid);
            statementEntity.setContextStatementId(contextStatementUid);
            statementEntity.setSubStatementActorUid(subActorUid);
            statementEntity.setSubstatementVerbUid(subVerbUid);
            statementEntity.setSubStatementObjectUid(subObjectUid);
            statementEntity.setTimestamp(UMCalendarUtil.parse8601Timestamp(statement.getTimestamp()).getTimeInMillis());
            statementEntity.setStored(UMCalendarUtil.parse8601Timestamp(statement.getStored()).getTimeInMillis());
            statementEntity.setFullStatement(gson.toJson(statement));
            if (statement.getResult() != null) {
                statementEntity.setResultCompletion(statement.getResult().isCompletion());
                statementEntity.setResultDuration(UMTinCanUtil.parse8601Duration(statement.getResult().getDuration()));
                statementEntity.setResultResponse(statement.getResult().getResponse());
                statementEntity.setResultSuccess(statement.getResult().isSuccess());
                if (statement.getResult().getScore() != null) {
                    statementEntity.setResultScoreMax(statement.getResult().getScore().getMax());
                    statementEntity.setResultScoreMin(statement.getResult().getScore().getMin());
                    statementEntity.setResultScoreScaled(statement.getResult().getScore().getScaled());
                    statementEntity.setResultScoreRaw(statement.getResult().getScore().getRaw());
                }
            }
            if (statement.getContext() != null) {
                statementEntity.setContextPlatform(statement.getContext().getPlatform());
                statementEntity.setContextRegistration(statement.getContext().getRegistration());
            }
            statementEntity.setStatementUid(dao.insert(statementEntity));
        }
        return statementEntity;
    }


}
