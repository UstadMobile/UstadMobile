package com.ustadmobile.core.contentformats.xapi.endpoints;

import com.google.gson.Gson;
import com.ustadmobile.core.contentformats.xapi.Actor;
import com.ustadmobile.core.contentformats.xapi.Attachment;
import com.ustadmobile.core.contentformats.xapi.ContextActivity;
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
import com.ustadmobile.lib.db.entities.AgentEntity;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.StatementEntity;
import com.ustadmobile.lib.db.entities.VerbEntity;
import com.ustadmobile.lib.db.entities.XObjectEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateAgent;
import static com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateContextStatementJoin;
import static com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.getPerson;
import static com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateStatementEntity;
import static com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateVerb;
import static com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateXObject;

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

        verbDao = db.getVerbDao();
        statementDao = db.getStatementDao();
        personDao = db.getPersonDao();
        xobjectDao = db.getXObjectDao();
        contextJoinDao = db.getContextXObjectStatementJoinDao();
        agentDao = db.getAgentDao();
    }


    public List<String> storeStatements(List<Statement> statements) throws IllegalArgumentException {

        List<String> statementUids = new ArrayList<>();
        for (Statement statement : statements) {
            StatementEntity entity = createStatement(statement);
            statementUids.add(entity.getStatementId());
        }
        return statementUids;
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

    public static void checkValidActor(Actor actor) throws IllegalArgumentException {

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

    public StatementEntity createStatement(Statement statement) throws IllegalArgumentException {

        checkValidStatement(statement, false);

        VerbEntity verbEntity = insertOrUpdateVerb(verbDao, statement.getVerb().getId());
        Person person = getPerson(personDao, statement.getActor());
        AgentEntity agentEntity = insertOrUpdateAgent(agentDao, personDao, statement.getActor());
        long agentUid = agentEntity.getAgentUid();

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
            AgentEntity authorityEntity = insertOrUpdateAgent(agentDao, personDao, authority);
            authorityUid = authorityEntity != null ? authorityEntity.getAgentUid() : 0;
        }

        XObjectEntity xObjectEntity = null;
        if (statement.getObject() != null) {
            xObjectEntity = insertOrUpdateXObject(xobjectDao, statement.getObject(), gson);
        }

        long subActorUid = 0;
        long subVerbUid = 0;
        long subObjectUid = 0;

        if (statement.getSubStatement() != null) {
            Statement subStatement = statement.getSubStatement();
            Person subActor = getPerson(personDao, subStatement.getActor());
            if (subActor == null) {
                AgentEntity subAgent = insertOrUpdateAgent(agentDao, personDao, statement.getSubStatement().getActor());
                subActorUid = subAgent.getAgentUid();
            }

            VerbEntity subVerb = insertOrUpdateVerb(verbDao, subStatement.getVerb().getId());
            subVerbUid = subVerb.getVerbUid();

            XObjectEntity subObject = insertOrUpdateXObject(xobjectDao, subStatement.getObject(), gson);
            subObjectUid = subObject.getXObjectUid();

        }

        String contextStatementId = "";
        long instructorUid = 0;
        long teamUid = 0;

        if (statement.getContext() != null) {

            if (statement.getContext().getInstructor() != null) {
                AgentEntity instructorAgent = insertOrUpdateAgent(agentDao, personDao, statement.getContext().getInstructor());
                instructorUid = instructorAgent.getAgentUid();
            }

            if (statement.getContext().getTeam() != null) {
                AgentEntity teamAgent = insertOrUpdateAgent(agentDao, personDao, statement.getContext().getTeam());
                teamUid = teamAgent.getAgentUid();
            }

            if (statement.getContext().getStatement() != null) {
                contextStatementId = statement.getContext().getStatement().getId();
            }
        }

        StatementEntity statementEntity = insertOrUpdateStatementEntity(statementDao, statement, gson,
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
            XObjectEntity xobjectEntity = insertOrUpdateXObject(xobjectDao, object, gson);
            insertOrUpdateContextStatementJoin(contextJoinDao, statementUid, xobjectEntity.getXObjectUid(), flag);
        }
    }

    public boolean hasMultipleStatementWithSameId(List<Statement> statementList) {
        Set<String> uniques = new HashSet<>();
        for (Statement statement : statementList) {

            if (statement.getId() != null) {
                boolean added = uniques.add(statement.getId());
                if (!added) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean hasExistingStatements(List<Statement> statements) {

        for (Statement statement : statements) {

            if (statement.getId() == null || statement.getId().isEmpty()) {
                continue;
            }

            StatementEntity statementEntity = statementDao.findByStatementId(statement.getId());
            if (statementEntity == null) {
                continue;
            }

            return true;

            // TODO statements can be updated in certain places
           /* Statement statementDb = gson.fromJson(statementEntity.getFullStatement(), Statement.class);

            if (!statementDb.equals(statement)) {
                return true;
            } */

        }

        return false;
    }
}
