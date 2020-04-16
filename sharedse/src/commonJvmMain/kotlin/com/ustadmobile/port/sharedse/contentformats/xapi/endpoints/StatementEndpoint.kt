package com.ustadmobile.port.sharedse.contentformats.xapi.endpoints

import com.google.gson.Gson
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.lib.db.entities.StatementEntity
import com.ustadmobile.lib.db.entities.XObjectEntity
import com.ustadmobile.port.sharedse.contentformats.xapi.Actor
import com.ustadmobile.port.sharedse.contentformats.xapi.Statement
import com.ustadmobile.port.sharedse.contentformats.xapi.XObject
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.getAgent
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.getPerson
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateContextStatementJoin
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateStatementEntity
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateVerb
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateVerbLangMap
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateXObject
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateXObjectLangMap
import java.text.SimpleDateFormat
import java.util.*

class StatementEndpoint(db: UmAppDatabase, private val gson: Gson) {
    private val verbDao: VerbDao = db.verbDao
    private val statementDao: StatementDao = db.statementDao
    private val personDao: PersonDao = db.personDao
    private val xobjectDao: XObjectDao = db.xObjectDao
    private val contextJoinDao: ContextXObjectStatementJoinDao = db.contextXObjectStatementJoinDao
    private val agentDao: AgentDao = db.agentDao
    private val xLangMapEntryDao: XLangMapEntryDao = db.xLangMapEntryDao
    private val languageDao: LanguageDao = db.languageDao
    private val langVariantDao = db.languageVariantDao
    private val contentEntryDao = db.contentEntryDao


    /**
     * @param contentEntryUid - the contentEntryUid when it is already known. E.g. when the xapi
     * endpoint is used by the XapiPackagePresenter then the contentEntryUid is known.
     */
    @Throws(IllegalArgumentException::class)
    fun storeStatements(statements: List<Statement>, statementId: String,
                        contentEntryUid: Long = 0L): List<String> {

        hasStatementWithMatchingId(statements, statementId)

        hasMultipleStatementWithSameId(statements)

        hasExistingStatements(statements)

        val statementUids = ArrayList<String>()
        for (statement in statements) {
            val entity = storeStatement(statement, contentEntryUid = contentEntryUid)
            statementUids.add(entity.statementId!!)
        }
        return statementUids
    }

    @Throws(IllegalArgumentException::class)
    private fun checkValidStatement(statement: Statement, isSubStatement: Boolean) {

        if (!isSubStatement && statement.id.isNullOrEmpty()) {
            statement.id = UUID.randomUUID().toString()
        }

        val actor = statement.actor
                ?: throw StatementRequestException("No Actor Found in Statement")

        checkValidActor(actor)

        val verb = statement.verb ?: throw StatementRequestException("No Verb Found in Statement")
        if (verb.id.isNullOrEmpty()) {
            throw IllegalArgumentException("Invalid Verb In Statement: Required Id not found")
        }

        val subStatement = statement.subStatement
        val xobject = statement.`object`
        if (subStatement == null && xobject == null) {
            throw StatementRequestException("No Object Found in Statement")
        }

        if (xobject != null) {
            if (xobject.id.isNullOrEmpty()) {
                throw StatementRequestException("Invalid Object In Statement: Required Id not found")
            }

            if (xobject.definition != null) {

                if (xobject.definition!!.type != null) {
                    if (xobject.definition!!.type == "http://adlnet.gov/expapi/activities/cmi.interaction") {

                        if (xobject.definition!!.interactionType.isNullOrEmpty()) {
                            throw StatementRequestException("Invalid Object In Statement: Required Interaction Type was not found")
                        }

                    }
                }

            }
        }

        val context = statement.context
        if (context != null) {

            if (xobject != null) {
                if (xobject.objectType != "Activity") {

                    if (context.revision != null) {
                        throw StatementRequestException("Invalid Context In Statement: Revision can only be used when objectType is activity")
                    }

                    if (context.platform != null) {
                        throw StatementRequestException("Invalid Context In Statement: Platform can only be used when objectType is activity")
                    }


                }

            }

            if (context.instructor != null) {
                checkValidActor(context.instructor!!)
            }

            if (context.team != null) {
                checkValidActor(context.team!!)
            }


        }

        if (subStatement != null) {

            if (subStatement.objectType == null) {
                throw StatementRequestException("Invalid Object In Statement: Required ObjectType was not found")
            }
            if (subStatement.id != null) {
                throw StatementRequestException("Invalid SubStatement In Statement: ID field is not required")
            }
            if (subStatement.stored != null) {
                throw StatementRequestException("Invalid SubStatement In Statement: stored field is not required")
            }
            if (subStatement.version != null) {
                throw StatementRequestException("Invalid SubStatement In Statement: version field is not required")
            }

            if (subStatement.authority != null) {
                throw StatementRequestException("Invalid SubStatement In Statement: authority object is not required")
            }

            if (subStatement.subStatement != null) {
                throw StatementRequestException("Invalid SubStatement In Statement: nested subStatement found")
            }

            checkValidStatement(subStatement, true)
        }


        if (statement.authority != null) {
            val authority = statement.authority
            checkValidActor(authority!!)
            if (authority.objectType.isNullOrEmpty()) {
                throw StatementRequestException("Invalid Authority In Statement: authority was not agent or group")
            }
            if (authority.objectType == "Group") {

                val membersList = authority.members
                if (membersList?.size != 2) {
                    throw StatementRequestException("Invalid Authority In Statement: invalid OAuth consumer")
                }

                var has1Account = false
                for (member in membersList) {
                    if (member.account != null) {
                        has1Account = actor.account?.homePage?.isNotEmpty() ?: false &&
                                actor.account?.name?.isNotEmpty() ?: false
                    }
                }
                if (!has1Account) {
                    throw StatementRequestException("Invalid Authority In Statement: does not have account for OAuth")
                }

            }


        }

        val attachmentList = statement.attachments

        if (attachmentList != null) {
            for (attachment in attachmentList) {

                if (attachment.usageType.isNullOrEmpty()) {
                    throw StatementRequestException("Invalid Attachment In Statement: Required usageType in Attachment not found")
                }

                if (attachment.display.isNullOrEmpty()) {
                    throw StatementRequestException("Invalid Attachment In Statement: Required displayMap in Attachment not found")
                }

                if (attachment.contentType.isNullOrEmpty()) {
                    throw StatementRequestException("Invalid Attachment In Statement: Required contentType in Attachment not found")
                }

                if (attachment.length == 0L) {
                    throw StatementRequestException("Invalid Attachment In Statement: Required length in Attachment not found")
                }

                if (attachment.sha2.isNullOrEmpty()) {
                    throw StatementRequestException("Invalid Attachment In Statement: Required sha2 in Attachment not found")
                }

            }
        }

        if (!isSubStatement) {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(Date())
            statement.stored = date

            if (statement.timestamp.isNullOrEmpty()) {
                statement.timestamp = date
            }

        }


    }

    @Throws(IllegalArgumentException::class)
    fun storeStatement(statement: Statement,
                       contentEntryUid: Long = 0L): StatementEntity {

        checkValidStatement(statement, false)

        val verbEntity = insertOrUpdateVerb(verbDao, statement.verb!!)
        val person = getPerson(personDao, statement.actor!!)
        val agentEntity = getAgent(agentDao, personDao, statement.actor!!)
        val agentUid = agentEntity.agentUid

        insertOrUpdateVerbLangMap(xLangMapEntryDao, statement.verb!!, verbEntity, languageDao, langVariantDao)

        var authorityUid: Long = 0
        if (statement.authority != null) {

            var authority = statement.authority
            if (statement.authority!!.objectType == "Group" && statement.authority!!.members!!.size == 2) {
                val memberList = statement.authority!!.members
                for (member in memberList!!) {
                    if (member.account == null) {
                        authority = member
                        break
                    }
                }
            }
            val authorityEntity = getAgent(agentDao, personDao, authority!!)
            authorityUid = authorityEntity.agentUid
        }

        var xObjectEntity: XObjectEntity? = null
        val xObjectVal = statement.`object`
        if (xObjectVal != null) {
            xObjectEntity = insertOrUpdateXObject(xobjectDao, xObjectVal, gson,
                    contentEntryDao, contentEntryUid)

            insertOrUpdateXObjectLangMap(xLangMapEntryDao, xObjectVal, xObjectEntity, languageDao, langVariantDao)
        }

        var subActorUid: Long = 0
        var subVerbUid: Long = 0
        var subObjectUid: Long = 0

        if (statement.subStatement != null) {
            val subStatement = statement.subStatement

            val subAgent = getAgent(agentDao, personDao, subStatement!!.actor!!)
            subActorUid = subAgent.agentUid

            val subVerb = insertOrUpdateVerb(verbDao, subStatement.verb!!)
            subVerbUid = subVerb.verbUid

            insertOrUpdateVerbLangMap(xLangMapEntryDao, subStatement.verb!!, subVerb, languageDao, langVariantDao)

            val subObject = insertOrUpdateXObject(xobjectDao, subStatement.`object`!!, gson,
                    contentEntryDao, contentEntryUid)

            subObjectUid = subObject.xObjectUid

            insertOrUpdateXObjectLangMap(xLangMapEntryDao, subStatement.`object`!!, subObject, languageDao, langVariantDao)

        }

        var contextStatementId = ""
        var instructorUid: Long = 0
        var teamUid: Long = 0

        val statementContext = statement.context
        if (statementContext != null) {

            val contextInstructor = statementContext.instructor
            if (contextInstructor != null) {
                val instructorAgent = getAgent(agentDao, personDao, contextInstructor)
                instructorUid = instructorAgent.agentUid
            }

            val contextTeam = statementContext.team
            if (contextTeam != null) {
                val teamAgent = getAgent(agentDao, personDao, contextTeam)
                teamUid = teamAgent.agentUid
            }

            contextStatementId = statementContext.statement?.id ?: ""
        }

        val statementEntity = insertOrUpdateStatementEntity(statementDao, statement, gson,
                person?.personUid ?: 0,
                verbEntity.verbUid,
                xObjectEntity?.xObjectUid ?: 0,
                contextStatementId, instructorUid,
                agentUid, authorityUid, teamUid,
                subActorUid, subVerbUid, subObjectUid,
                contentEntryUid = contentEntryUid)

        if (statement.context != null && statement.context!!.contextActivities != null) {

            val contextActivity = statement.context!!.contextActivities
            if (contextActivity!!.parent != null) {
                createAllContextActivities(contextActivity.parent,
                        statementEntity.statementUid, ContextXObjectStatementJoinDao.CONTEXT_FLAG_PARENT)
            }

            if (contextActivity.category != null) {
                createAllContextActivities(contextActivity.category,
                        statementEntity.statementUid, ContextXObjectStatementJoinDao.CONTEXT_FLAG_CATEGORY)
            }
            if (contextActivity.grouping != null) {
                createAllContextActivities(contextActivity.grouping,
                        statementEntity.statementUid, ContextXObjectStatementJoinDao.CONTEXT_FLAG_GROUPING)
            }
            if (contextActivity.other != null) {
                createAllContextActivities(contextActivity.other,
                        statementEntity.statementUid, ContextXObjectStatementJoinDao.CONTEXT_FLAG_OTHER)
            }

        }
        return statementEntity
    }

    fun createAllContextActivities(list: List<XObject>?, statementUid: Long, flag: Int) {
        for (`object` in list!!) {
            val xobjectEntity = insertOrUpdateXObject(xobjectDao, `object`, gson, contentEntryDao)
            insertOrUpdateContextStatementJoin(contextJoinDao, statementUid, xobjectEntity.xObjectUid, flag)
        }
    }

    @Throws(StatementRequestException::class)
    private fun hasStatementWithMatchingId(statements: List<Statement>, statementId: String) {

        if (statementId.isEmpty()) {
            return
        }
        for (statement in statements) {
            if (statementId != statement.id) {
                throw StatementRequestException("Statement Id did not match with Parameter Statement ID", 409)
            }
        }
    }

    @Throws(StatementRequestException::class)
    fun hasMultipleStatementWithSameId(statementList: List<Statement>): Boolean {
        val uniques = HashSet<String>()
        for (statement in statementList) {

            if (statement.id != null) {
                val added = uniques.add(statement.id!!)
                if (!added) {
                    throw StatementRequestException("Multiple Statements With Same Id")
                }
            }
        }
        return false
    }

    @Throws(StatementRequestException::class)
    fun hasExistingStatements(statements: List<Statement>): Boolean {

        val ids = statements.filter { it.id != null }.map { it.id }
        val statementList = statementDao.findByStatementIdList(ids as List<String>)

        for (statement in statementList) {
            throw StatementRequestException("Has Existing Statements", 409)

            // TODO statements can be updated in certain places
            /* Statement statementDb = gson.fromJson(statementEntity.getFullStatement(), Statement.class);

            if (!statementDb.equals(statement)) {
                return true;
            } */

        }

        return false
    }

    companion object {

        const val EXTENSION_PROGRESS = "https://w3id.org/xapi/cmi5/result/extensions/progress"

        @Throws(StatementRequestException::class)
        fun checkValidActor(actor: Actor) {

            val hasMbox = actor.mbox?.isNotEmpty() ?: false
            val hasSha = actor.mbox_sha1sum?.isNotEmpty() ?: false
            val hasOpenId = actor.openid?.isNotEmpty() ?: false
            val hasAccount = actor.account?.homePage?.isNotEmpty() ?: false &&
                    actor.account?.name?.isNotEmpty() ?: false

            val idCount = (if (hasAccount) 1 else 0) + (if (hasMbox) 1 else 0) + (if (hasOpenId) 1 else 0) + if (hasSha) 1 else 0
            if (actor.objectType == null || actor.objectType == "Agent") {

                if (idCount == 0) {
                    throw StatementRequestException("Invalid Actor In Statement: Required Id not found")
                }
                if (idCount > 1) {
                    throw StatementRequestException("More than 1 Id identified in Actor")
                }

            } else if (actor.objectType == "Group") {

                if (idCount == 0 && actor.members == null) {
                    throw StatementRequestException("Invalid Actor In Statement: Required list of members not found for group")
                }

                if (idCount > 1) {
                    throw StatementRequestException("More than 1 Id identified in Actor")
                }

                if (actor.members != null) {
                    for (members in actor.members!!) {
                        checkValidActor(members)
                        if (members.members?.isNotEmpty() == true) {
                            throw StatementRequestException("Members were found in the member group statement")
                        }
                    }
                }
            }

        }
    }
}
