package com.ustadmobile.core.contentformats.xapi.endpoints;

import com.google.gson.Gson;
import com.ustadmobile.core.contentformats.xapi.Actor;
import com.ustadmobile.core.contentformats.xapi.Attachment;
import com.ustadmobile.core.contentformats.xapi.ContextActivity;
import com.ustadmobile.core.contentformats.xapi.Definition;
import com.ustadmobile.core.contentformats.xapi.Statement;
import com.ustadmobile.core.contentformats.xapi.Verb;
import com.ustadmobile.core.contentformats.xapi.XContext;
import com.ustadmobile.core.contentformats.xapi.XObject;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.AgentDao;
import com.ustadmobile.core.db.dao.ContextXObjectStatementJoinDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.StatementDao;
import com.ustadmobile.core.db.dao.VerbDao;
import com.ustadmobile.core.db.dao.XObjectDao;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.lib.db.entities.AgentEntity;
import com.ustadmobile.lib.db.entities.ContextXObjectStatementJoin;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.StatementEntity;
import com.ustadmobile.lib.db.entities.VerbEntity;
import com.ustadmobile.lib.db.entities.XObjectEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class StatementEndpoint {

    private Gson gson;
    private UmAppDatabase db;
    private VerbDao verbDao;
    private StatementDao statementDao;
    private PersonDao personDao;
    private XObjectDao xobjectDao;
    private ContextXObjectStatementJoinDao contextJoinDao;
    private AgentDao agentDao;

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
        agentDao = db.getAgentDao();

        for (Statement statement : statements) {
            createStatement(statement);
        }
    }

    private void checkValidStatement(Statement statement, boolean isSubStatement) throws IllegalArgumentException {

        if (!isSubStatement && (statement.getId() == null || statement.getId().isEmpty())) {
            statement.setId(UUID.randomUUID().toString());
        }

        Actor actor = statement.getActor();
        if (actor == null) {
            throw new IllegalArgumentException("No Actor Found in Statement");
        }

        checkValidActor(actor);

        Verb verb = statement.getVerb();
        if (verb == null) {
            throw new IllegalArgumentException("No Verb Found in Statement");
        }
        if (verb.getId() == null || verb.getId().isEmpty()) {
            throw new IllegalArgumentException("Invalid Verb In Statement: Required Id not found");
        }


        Statement subStatement = statement.getSubStatement();
        XObject xobject = statement.getObject();
        if (subStatement == null && xobject == null) {
            throw new IllegalArgumentException("No Object Found in Statement");
        }

        if (xobject != null) {
            if (xobject.getId() == null || xobject.getId().isEmpty()) {
                throw new IllegalArgumentException("Invalid Object In Statement: Required Id not found");
            }

            if (xobject.getDefinition() != null) {

                if (xobject.getDefinition().getType() != null) {
                    if (xobject.getDefinition().getType().equals("http://adlnet.gov/expapi/activities/cmi.interaction")) {

                        if (xobject.getDefinition().getInteractionType() == null || xobject.getDefinition().getInteractionType().isEmpty()) {
                            throw new IllegalArgumentException("Invalid Object In Statement: Required Interaction Type was not found");
                        }

                    }
                }

            }
        }

        XContext context = statement.getContext();
        if (context != null) {

            if (xobject != null) {

                if (xobject.getObjectType() != null && !xobject.getObjectType().equals("Activity")) {

                    if (context.getRevision() != null) {
                        throw new IllegalArgumentException("Invalid Context In Statement: Revision can only be used when objectType is activity");
                    }

                    if (context.getPlatform() != null) {
                        throw new IllegalArgumentException("Invalid Context In Statement: Platform can only be used when objectType is activity");
                    }


                }

            }

            if (context.getInstructor() != null) {
                checkValidActor(context.getInstructor());
            }

            if (context.getTeam() != null) {
                checkValidActor(context.getTeam());
            }


        }

        if (subStatement != null) {

            if (subStatement.getObjectType() == null) {
                throw new IllegalArgumentException("Invalid Object In Statement: Required ObjectType was not found");
            }
            if (subStatement.getId() != null) {
                throw new IllegalArgumentException("Invalid SubStatement In Statement: ID field is not required");
            }
            if (subStatement.getStored() != null) {
                throw new IllegalArgumentException("Invalid SubStatement In Statement: stored field is not required");
            }
            if (subStatement.getVersion() != null) {
                throw new IllegalArgumentException("Invalid SubStatement In Statement: version field is not required");
            }

            if (subStatement.getAuthority() != null) {
                throw new IllegalArgumentException("Invalid SubStatement In Statement: authority object is not required");
            }

            if (subStatement.getSubStatement() != null) {
                throw new IllegalArgumentException("Invalid SubStatement In Statement: nested subStatement found");
            }

            checkValidStatement(subStatement, true);
        }


        if (statement.getAuthority() != null) {
            Actor authority = statement.getAuthority();
            checkValidActor(authority);
            if (authority.getObjectType() == null || authority.getObjectType().isEmpty()) {
                throw new IllegalArgumentException("Invalid Authority In Statement: authority was not agent or group");
            }
            if (authority.getObjectType().equals("Group")) {

                List<Actor> membersList = authority.getMembers();
                if (membersList.size() != 2) {
                    throw new IllegalArgumentException("Invalid Authority In Statement: invalid OAuth consumer");
                }

                boolean has1Account = false;
                for (Actor member : membersList) {
                    if (member.getAccount() != null) {
                        has1Account = actor.getAccount().getHomePage() != null && !actor.getAccount().getHomePage().isEmpty() &&
                                actor.getAccount().getName() != null && !actor.getAccount().getName().isEmpty();
                    }
                }
                if (!has1Account) {
                    throw new IllegalArgumentException("Invalid Authority In Statement: does not have account for OAuth");
                }

            }


        }

        List<Attachment> attachmentList = statement.getAttachments();

        if (attachmentList != null) {
            for (Attachment attachment : attachmentList) {

                if (attachment.getUsageType() == null || attachment.getUsageType().isEmpty()) {
                    throw new IllegalArgumentException("Invalid Attachment In Statement: Required usageType in Attachment not found");
                }

                if (attachment.getDisplay() == null || attachment.getDisplay().size() > 0) {
                    throw new IllegalArgumentException("Invalid Attachment In Statement: Required displayMap in Attachment not found");
                }

                if (attachment.getContentType() == null || attachment.getContentType().isEmpty()) {
                    throw new IllegalArgumentException("Invalid Attachment In Statement: Required contentType in Attachment not found");
                }

                if (attachment.getLength() == 0) {
                    throw new IllegalArgumentException("Invalid Attachment In Statement: Required length in Attachment not found");
                }

                if (attachment.getSha2() == null || attachment.getSha2().isEmpty()) {
                    throw new IllegalArgumentException("Invalid Attachment In Statement: Required sha2 in Attachment not found");
                }

            }
        }

        if (!isSubStatement) {
            String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz").format(new Date());
            statement.setStored(date);

            if (statement.getTimestamp() == null || statement.getTimestamp().isEmpty()) {
                statement.setTimestamp(date);
            }

        }


    }

    private void checkValidActor(Actor actor) {

        boolean hasMbox = actor.getMbox() != null && !actor.getMbox().isEmpty();
        boolean hasSha = actor.getMbox_sha1sum() != null && !actor.getMbox_sha1sum().isEmpty();
        boolean hasOpenId = actor.getOpenid() != null && !actor.getOpenid().isEmpty();
        boolean hasAccount = actor.getAccount() != null &&
                actor.getAccount().getHomePage() != null && !actor.getAccount().getHomePage().isEmpty() &&
                actor.getAccount().getName() != null && !actor.getAccount().getName().isEmpty();

        int idCount = (hasAccount ? 1 : 0) + (hasMbox ? 1 : 0) + (hasOpenId ? 1 : 0) + (hasSha ? 1 : 0);
        if (actor.getObjectType() == null || actor.getObjectType().equals("Agent")) {

            if (idCount == 0) {
                throw new IllegalArgumentException("Invalid Actor In Statement: Required Id not found");
            }
            if (idCount > 1) {
                throw new IllegalArgumentException("More than 1 Id identified in Actor");
            }

        } else if (actor.getObjectType().equals("Group")) {

            if (idCount == 0 && actor.getMembers() == null) {
                throw new IllegalArgumentException("Invalid Actor In Statement: Required list of members not found for group");
            }

            if (idCount > 1) {
                throw new IllegalArgumentException("More than 1 Id identified in Actor");
            }

            if (actor.getMembers() != null) {
                for (Actor members : actor.getMembers()) {
                    checkValidActor(members);
                    if (members.getMembers() != null && members.getMembers().size() > 0) {
                        throw new IllegalArgumentException("Members were found in the member group statement");
                    }
                }
            }
        }

    }

    public StatementEntity createStatement(Statement statement) {

        checkValidStatement(statement, false);

        VerbEntity verbEntity = insertOrUpdateVerb(verbDao, statement.getVerb().getId());
        Person person = insertOrUpdatePerson(personDao, statement.getActor());
        long agentUid = 0;
        if (person == null) {
            AgentEntity agentEntity = insertOrUpdateAgent(agentDao, statement.getActor());
            agentUid = agentEntity.getAgentUid();
        }

        long authorityUid = 0;
        if (statement.getAuthority() != null) {

            Actor authority = statement.getAuthority();
            if (statement.getAuthority().getObjectType().equals("Group") && statement.getAuthority().getMembers().size() == 2) {
                List<Actor> memberList = statement.getAuthority().getMembers();
                for (Actor member : memberList) {
                    if (member.getAccount() == null) {
                        authority = member;
                        break;
                    }
                }
            }
            AgentEntity agentEntity = insertOrUpdateAgent(agentDao, authority);
            authorityUid = agentEntity != null ? agentEntity.getAgentUid() : 0;
        }

        XObjectEntity xObjectEntity = null;
        if (statement.getObject() != null) {
            xObjectEntity = insertOrUpdateXObject(xobjectDao, statement.getObject());
        }

        long subActorUid = 0;
        long subVerbUid = 0;
        long subObjectUid = 0;

        if (statement.getSubStatement() != null) {
            Statement subStatement = statement.getSubStatement();
            Person subActor = insertOrUpdatePerson(personDao, subStatement.getActor());
            if (subActor == null) {
                AgentEntity agentEntity = insertOrUpdateAgent(agentDao, statement.getSubStatement().getActor());
                subActorUid = agentEntity.getAgentUid();
            }

            VerbEntity subVerb = insertOrUpdateVerb(verbDao, subStatement.getVerb().getId());
            subVerbUid = subVerb.getVerbUid();

            XObjectEntity subObject = insertOrUpdateXObject(xobjectDao, subStatement.getObject());
            subObjectUid = subObject.getXObjectUid();

        }

        String contextStatementId = "";
        long instructorUid = 0;
        long teamUid = 0;

        if (statement.getContext() != null) {

            if (statement.getContext().getInstructor() != null) {
                AgentEntity agentEntity = insertOrUpdateAgent(agentDao, statement.getContext().getInstructor());
                instructorUid = agentEntity.getAgentUid();
            }

            if (statement.getContext().getTeam() != null) {
                AgentEntity agentEntity = insertOrUpdateAgent(agentDao, statement.getContext().getTeam());
                teamUid = agentEntity.getAgentUid();
            }

            if (statement.getContext().getStatement() != null) {
                contextStatementId = statement.getContext().getStatement().getId();
            }
        }

        StatementEntity statementEntity = insertOrUpdateStatementEntity(statementDao, statement,
                person != null ? person.getPersonUid() : 0,
                verbEntity != null ? verbEntity.getVerbUid() : 0,
                xObjectEntity != null ? xObjectEntity.getXObjectUid() : 0,
                contextStatementId, instructorUid,
                agentUid, authorityUid, teamUid,
                subActorUid, subVerbUid, subObjectUid);

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

    public AgentEntity insertOrUpdateAgent(AgentDao dao, Actor actor) {
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
            agentEntity.setAgentUid(dao.insert(agentEntity));
        }

        return agentEntity;
    }


    public VerbEntity insertOrUpdateVerb(VerbDao dao, String urlId) {

        VerbEntity verbEntity = dao.findByUrl(urlId);
        if (verbEntity == null) {
            verbEntity = new VerbEntity();
            verbEntity.setUrlId(urlId);
            verbEntity.setVerbUid(dao.insert(verbEntity));
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

    public StatementEntity insertOrUpdateStatementEntity(StatementDao dao, Statement statement,
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
