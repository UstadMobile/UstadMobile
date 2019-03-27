package com.ustadmobile.core.contentformats.xapi.endpoints;

import com.google.gson.Gson;
import com.ustadmobile.core.contentformats.xapi.Actor;
import com.ustadmobile.core.contentformats.xapi.ContextActivity;
import com.ustadmobile.core.contentformats.xapi.Definition;
import com.ustadmobile.core.contentformats.xapi.Statement;
import com.ustadmobile.core.contentformats.xapi.XObject;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContextXObjectStatementJoinDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.StatementDao;
import com.ustadmobile.core.db.dao.VerbDao;
import com.ustadmobile.core.db.dao.XObjectDao;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.lib.db.entities.ContextXObjectStatementJoin;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.StatementEntity;
import com.ustadmobile.lib.db.entities.VerbEntity;
import com.ustadmobile.lib.db.entities.XObjectEntity;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class StatementEndpoint {

    private Gson gson;
    private UmAppDatabase db;
    private VerbDao verbDao;
    private StatementDao statementDao;
    private PersonDao personDao;
    private XObjectDao xobjectDao;
    private ContextXObjectStatementJoinDao contextJoinDao;

    public StatementEndpoint(UmAppDatabase db, Gson gson) {
        this.db = db;
        this.gson = gson;
    }


    public void storeStatements(List<Statement> statements) {

        verbDao = db.getVerbDao();
        statementDao = db.getStatementDao();
        personDao = db.getPersonDao();
        xobjectDao = db.getXObjectDao();
        contextJoinDao = db.getContextXObjectStatementJoinDao();

        for (Statement statement : statements) {
            createStatement(statement);
        }
    }

    public StatementEntity createStatement(Statement statement) {

        VerbEntity verbEntity = insertOrUpdateVerb(verbDao, statement.getVerb().getId());
        Person person = insertOrUpdatePerson(personDao, statement.getActor());
        XObjectEntity xObjectEntity = insertOrUpdateXObject(xobjectDao, statement.getObject());
        long subStatementUid = 0;
        if (statement.getSubStatement() != null) {
            StatementEntity subStatement = createStatement(statement.getSubStatement());
            subStatementUid = subStatement.getStatementUid();
        }
        long contextStatementUid = 0;
        if (statement.getContext() != null && statement.getContext().getStatement() != null) {
            String contextStatementId = statement.getContext().getStatement().getId();
            StatementEntity entity = statementDao.findByStatementId(contextStatementId);
            contextStatementUid = entity.getStatementUid();
        }

        StatementEntity statementEntity = insertOrUpdateStatementEntity(statementDao, statement,
                person != null ? person.getPersonUid() : 0,
                verbEntity != null ? verbEntity.getVerbUid() : 0,
                xObjectEntity != null ? xObjectEntity.getXObjectUid() : 0,
                subStatementUid, contextStatementUid);

        if (statement.getContext() != null && statement.getContext().getContextActivities() != null) {

            ContextActivity contextActivity = statement.getContext().getContextActivities();
            if (contextActivity.getParent() != null) {
                createAllContextActivities(contextActivity.getParent(),
                        statementEntity.getStatementUid(), ContextXObjectStatementJoinDao.CONTEXT_FLAG_PARENT);
            }

            if (contextActivity.getCategory() != null) {
                createAllContextActivities(contextActivity.getCategory(),
                        statementEntity.getStatementUid(), ContextXObjectStatementJoinDao.CONTEXT_FLAG_CATEGORY);
            }
            if (contextActivity.getGrouping() != null) {
                createAllContextActivities(contextActivity.getGrouping(),
                        statementEntity.getStatementUid(), ContextXObjectStatementJoinDao.CONTEXT_FLAG_GROUPING);
            }
            if (contextActivity.getOther() != null) {
                createAllContextActivities(contextActivity.getOther(),
                        statementEntity.getStatementUid(), ContextXObjectStatementJoinDao.CONTEXT_FLAG_OTHER);
            }

        }
        return statementEntity;
    }

    public void createAllContextActivities(List<XObject> list, long statementUid, int flag) {
        for (XObject object : list) {
            XObjectEntity xobjectEntity = insertOrUpdateXObject(xobjectDao, object);
            insertOrUpdateContextStatementJoin(contextJoinDao, statementUid, xobjectEntity.getXObjectUid(), flag);
        }
    }


    public VerbEntity insertOrUpdateVerb(VerbDao dao, String urlId) {

        VerbEntity verbEntity = dao.findByUrl(urlId);
        if (verbEntity == null) {
            verbEntity = new VerbEntity();
            verbEntity.setUrlId(urlId);
            dao.insert(verbEntity);
        }
        return verbEntity;

    }

    public ContextXObjectStatementJoin insertOrUpdateContextStatementJoin(ContextXObjectStatementJoinDao dao, long statementUid, long objectUid, int flag) {

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

    public Person insertOrUpdatePerson(PersonDao dao, Actor actor) {
        Person person = null;
        if (actor.getAccount() != null) {
            person = dao.findByUsername(actor.getAccount().getName());
            if (person == null) {
                person = new Person();
                person.setUsername(actor.getAccount().getName());
                person.setPersonUid(dao.insert(person));
            }
        }
        return person;
    }

    public XObjectEntity insertOrUpdateXObject(XObjectDao dao, XObject xobject) {

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
                xObjectEntity.setDefinitionType(StringUtils.isNotEmpty(changedDefinition.getType()) ?
                        changedDefinition.getType() : entity.getDefinitionType());

                xObjectEntity.setInteractionType(StringUtils.isNotEmpty(changedDefinition.getInteractionType()) ?
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

    public StatementEntity insertOrUpdateStatementEntity(StatementDao dao, Statement statement,
                                                         long personUid, long verbUid, long objectUid, long substatementuid, long contextStatementUid) {

        StatementEntity statementEntity = dao.findByStatementId(statement.getId());
        if (statementEntity == null) {
            statementEntity = new StatementEntity();
            statementEntity.setPersonUid(personUid);
            statementEntity.setStatementId(statement.getId());
            statementEntity.setVerbUid(verbUid);
            statementEntity.setXObjectUid(objectUid);
            statementEntity.setContextStatementUid(contextStatementUid);
            statementEntity.setSubStatementUid(substatementuid);
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
        }
        return statementEntity;
    }


}
