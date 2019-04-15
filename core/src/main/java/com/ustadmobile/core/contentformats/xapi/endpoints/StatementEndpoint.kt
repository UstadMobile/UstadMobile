package com.ustadmobile.core.contentformats.xapi.endpoints

import com.google.gson.Gson
import com.ustadmobile.core.contentformats.xapi.Actor
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.contentformats.xapi.XObject
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.getAgent
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.getPerson
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateContextStatementJoin
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateStatementEntity
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateVerb
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateXObject
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.lib.db.entities.StatementEntity
import com.ustadmobile.lib.db.entities.XObjectEntity
import java.text.SimpleDateFormat
import java.util.*

class StatementEndpoint(db: UmAppDatabase, private val gson: Gson) {
    private val verbDao: VerbDao = db.verbDao
    private val statementDao: StatementDao = db.statementDao
    private val personDao: PersonDao = db.personDao
    private val xobjectDao: XObjectDao = db.xObjectDao
    private val contextJoinDao: ContextXObjectStatementJoinDao = db.contextXObjectStatementJoinDao
    private val agentDao: AgentDao = db.agentDao


    @Throws(IllegalArgumentException::class)
    fun storeStatements(statements: List<Statement>): List<String> {

        val statementUids = ArrayList<String>()
        for (statement in statements) {
            val entity = createStatement(statement)
            statementUids.add(entity.statementId)
        }
        return statementUids
    }

    @Throws(IllegalArgumentException::class)
    private fun checkValidStatement(statement: Statement, isSubStatement: Boolean) {

        if (!isSubStatement && (statement.id == null || statement.id!!.isEmpty())) {
            statement.id = UUID.randomUUID().toString()
        }

        val actor = statement.actor ?: throw IllegalArgumentException("No Actor Found in Statement")

        checkValidActor(actor)

        val verb = statement.verb ?: throw IllegalArgumentException("No Verb Found in Statement")
        if (verb.id == null || verb.id!!.isEmpty()) {
            throw IllegalArgumentException("Invalid Verb In Statement: Required Id not found")
        }


        val subStatement = statement.subStatement
        val xobject = statement.`object`
        if (subStatement == null && xobject == null) {
            throw IllegalArgumentException("No Object Found in Statement")
        }

        if (xobject != null) {
            if (xobject.id == null || xobject.id!!.isEmpty()) {
                throw IllegalArgumentException("Invalid Object In Statement: Required Id not found")
            }

            if (xobject.definition != null) {

                if (xobject.definition!!.type != null) {
                    if (xobject.definition!!.type == "http://adlnet.gov/expapi/activities/cmi.interaction") {

                        if (xobject.definition!!.interactionType == null || xobject.definition!!.interactionType!!.isEmpty()) {
                            throw IllegalArgumentException("Invalid Object In Statement: Required Interaction Type was not found")
                        }

                    }
                }

            }
        }

        val context = statement.context
        if (context != null) {

            if (xobject != null) {

                if (xobject.objectType != null && xobject.objectType != "Activity") {

                    if (context.revision != null) {
                        throw IllegalArgumentException("Invalid Context In Statement: Revision can only be used when objectType is activity")
                    }

                    if (context.platform != null) {
                        throw IllegalArgumentException("Invalid Context In Statement: Platform can only be used when objectType is activity")
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
                throw IllegalArgumentException("Invalid Object In Statement: Required ObjectType was not found")
            }
            if (subStatement.id != null) {
                throw IllegalArgumentException("Invalid SubStatement In Statement: ID field is not required")
            }
            if (subStatement.stored != null) {
                throw IllegalArgumentException("Invalid SubStatement In Statement: stored field is not required")
            }
            if (subStatement.version != null) {
                throw IllegalArgumentException("Invalid SubStatement In Statement: version field is not required")
            }

            if (subStatement.authority != null) {
                throw IllegalArgumentException("Invalid SubStatement In Statement: authority object is not required")
            }

            if (subStatement.subStatement != null) {
                throw IllegalArgumentException("Invalid SubStatement In Statement: nested subStatement found")
            }

            checkValidStatement(subStatement, true)
        }


        if (statement.authority != null) {
            val authority = statement.authority
            checkValidActor(authority!!)
            if (authority.objectType == null || authority.objectType!!.isEmpty()) {
                throw IllegalArgumentException("Invalid Authority In Statement: authority was not agent or group")
            }
            if (authority.objectType == "Group") {

                val membersList = authority.members
                if (membersList!!.size != 2) {
                    throw IllegalArgumentException("Invalid Authority In Statement: invalid OAuth consumer")
                }

                var has1Account = false
                for (member in membersList) {
                    if (member.account != null) {
                        has1Account = actor.account!!.homePage != null && !actor.account!!.homePage!!.isEmpty() &&
                                actor.account!!.name != null && !actor.account!!.name!!.isEmpty()
                    }
                }
                if (!has1Account) {
                    throw IllegalArgumentException("Invalid Authority In Statement: does not have account for OAuth")
                }

            }


        }

        val attachmentList = statement.attachments

        if (attachmentList != null) {
            for (attachment in attachmentList) {

                if (attachment.usageType == null || attachment.usageType!!.isEmpty()) {
                    throw IllegalArgumentException("Invalid Attachment In Statement: Required usageType in Attachment not found")
                }

                if (attachment.display == null || attachment.display!!.size > 0) {
                    throw IllegalArgumentException("Invalid Attachment In Statement: Required displayMap in Attachment not found")
                }

                if (attachment.contentType == null || attachment.contentType!!.isEmpty()) {
                    throw IllegalArgumentException("Invalid Attachment In Statement: Required contentType in Attachment not found")
                }

                if (attachment.length == 0L) {
                    throw IllegalArgumentException("Invalid Attachment In Statement: Required length in Attachment not found")
                }

                if (attachment.sha2 == null || attachment.sha2!!.isEmpty()) {
                    throw IllegalArgumentException("Invalid Attachment In Statement: Required sha2 in Attachment not found")
                }

            }
        }

        if (!isSubStatement) {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz").format(Date())
            statement.stored = date

            if (statement.timestamp == null || statement.timestamp!!.isEmpty()) {
                statement.timestamp = date
            }

        }


    }

    @Throws(IllegalArgumentException::class)
    fun createStatement(statement: Statement): StatementEntity {

        checkValidStatement(statement, false)

        val verbEntity = insertOrUpdateVerb(verbDao, statement.verb!!)
        val person = getPerson(personDao, statement.actor!!)
        val agentEntity = getAgent(agentDao, personDao, statement.actor!!)
        val agentUid = agentEntity.agentUid

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
            authorityUid = authorityEntity?.agentUid ?: 0
        }

        var xObjectEntity: XObjectEntity? = null
        if (statement.`object` != null) {
            xObjectEntity = insertOrUpdateXObject(xobjectDao, statement.`object`!!, gson)
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

            val subObject = insertOrUpdateXObject(xobjectDao, subStatement.`object`!!, gson)
            subObjectUid = subObject.xObjectUid

        }

        var contextStatementId = ""
        var instructorUid: Long = 0
        var teamUid: Long = 0

        if (statement.context != null) {

            if (statement.context!!.instructor != null) {
                val instructorAgent = getAgent(agentDao, personDao, statement.context!!.instructor!!)
                instructorUid = instructorAgent.agentUid
            }

            if (statement.context!!.team != null) {
                val teamAgent = getAgent(agentDao, personDao, statement.context!!.team!!)
                teamUid = teamAgent.agentUid
            }

            if (statement.context!!.statement != null) {
                contextStatementId = statement.context!!.statement!!.id!!
            }
        }

        val statementEntity = insertOrUpdateStatementEntity(statementDao, statement, gson,
                person?.personUid ?: 0,
                verbEntity?.verbUid ?: 0,
                xObjectEntity?.xObjectUid ?: 0,
                contextStatementId, instructorUid,
                agentUid, authorityUid, teamUid,
                subActorUid, subVerbUid, subObjectUid)

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
            val xobjectEntity = insertOrUpdateXObject(xobjectDao, `object`, gson)
            insertOrUpdateContextStatementJoin(contextJoinDao, statementUid, xobjectEntity.xObjectUid, flag)
        }
    }

    fun hasMultipleStatementWithSameId(statementList: List<Statement>): Boolean {
        val uniques = HashSet<String>()
        for (statement in statementList) {

            if (statement.id != null) {
                val added = uniques.add(statement.id!!)
                if (!added) {
                    return true
                }
            }
        }
        return false
    }


    fun hasExistingStatements(statements: List<Statement>): Boolean {

        for (statement in statements) {

            if (statement.id == null || statement.id!!.isEmpty()) {
                continue
            }

            val statementEntity = statementDao.findByStatementId(statement.id) ?: continue

            return true

            // TODO statements can be updated in certain places
            /* Statement statementDb = gson.fromJson(statementEntity.getFullStatement(), Statement.class);

            if (!statementDb.equals(statement)) {
                return true;
            } */

        }

        return false
    }

    companion object {

        @Throws(IllegalArgumentException::class)
        fun checkValidActor(actor: Actor) {

            val hasMbox = actor.mbox != null && !actor.mbox!!.isEmpty()
            val hasSha = actor.mbox_sha1sum != null && !actor.mbox_sha1sum!!.isEmpty()
            val hasOpenId = actor.openid != null && !actor.openid!!.isEmpty()
            val hasAccount = actor.account != null &&
                    actor.account!!.homePage != null && !actor.account!!.homePage!!.isEmpty() &&
                    actor.account!!.name != null && !actor.account!!.name!!.isEmpty()

            val idCount = (if (hasAccount) 1 else 0) + (if (hasMbox) 1 else 0) + (if (hasOpenId) 1 else 0) + if (hasSha) 1 else 0
            if (actor.objectType == null || actor.objectType == "Agent") {

                if (idCount == 0) {
                    throw IllegalArgumentException("Invalid Actor In Statement: Required Id not found")
                }
                if (idCount > 1) {
                    throw IllegalArgumentException("More than 1 Id identified in Actor")
                }

            } else if (actor.objectType == "Group") {

                if (idCount == 0 && actor.members == null) {
                    throw IllegalArgumentException("Invalid Actor In Statement: Required list of members not found for group")
                }

                if (idCount > 1) {
                    throw IllegalArgumentException("More than 1 Id identified in Actor")
                }

                if (actor.members != null) {
                    for (members in actor.members!!) {
                        checkValidActor(members)
                        if (members.members != null && members.members!!.size > 0) {
                            throw IllegalArgumentException("Members were found in the member group statement")
                        }
                    }
                }
            }

        }
    }
}
