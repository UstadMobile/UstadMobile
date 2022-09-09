package com.ustadmobile.port.sharedse.contentformats.xapi.endpoints

import com.google.gson.Gson
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.xapi.Actor
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.contentformats.xapi.XObject
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.lib.db.entities.StatementEntity
import com.ustadmobile.lib.db.entities.XObjectEntity
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.getAgent
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.getPerson
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateContextStatementJoin
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateStatementEntity
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateVerb
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateVerbLangMap
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateXObject
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiUtil.insertOrUpdateXObjectLangMap
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import java.text.SimpleDateFormat
import java.util.*
import com.ustadmobile.door.ext.DoorTag

class XapiStatementEndpointImpl(val endpoint: Endpoint, override val di: DI) : XapiStatementEndpoint {

    private val db: UmAppDatabase by on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val repo: UmAppDatabase by on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val gson: Gson by di.instance()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    private val timeZone = TimeZone.getTimeZone("UTC")

    init {
        dateFormat.timeZone = timeZone
    }

    /**
     * @param contentEntryUid - the contentEntryUid when it is already known. E.g. when the xapi
     * endpoint is used by the XapiPackagePresenter then the contentEntryUid is known.
     */
    @Throws(IllegalArgumentException::class)
    override fun storeStatements(statements: List<Statement>, statementId: String,
                                 contentEntryUid: Long, clazzUid: Long): List<String> {

        hasStatementWithMatchingId(statements, statementId)

        hasMultipleStatementWithSameId(statements)

        hasExistingStatements(statements)

        val statementUids = ArrayList<String>()
        for (statement in statements) {
            val entity = storeStatement(statement,
                    contentEntryUid = contentEntryUid, clazzUid = clazzUid)
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
            val date = dateFormat.format(Date())
            statement.stored = date

            if (statement.timestamp.isNullOrEmpty()) {
                statement.timestamp = date
            }

        }


    }

    @Throws(IllegalArgumentException::class)
    fun storeStatement(statement: Statement,
                       contentEntryUid: Long = 0L,
                        clazzUid: Long = 0L): StatementEntity {

        checkValidStatement(statement, false)

        val verbEntity = insertOrUpdateVerb(repo.verbDao, statement.verb!!)
        val person = getPerson(repo.personDao, statement.actor!!)
        val agentEntity = getAgent(repo.agentDao, repo.personDao, statement.actor!!)
        var learnerGroupUid: Long = 0
        if(agentEntity.agentAccountName?.contains("group:") == true){
            learnerGroupUid = agentEntity.agentAccountName?.substringAfter(":")?.toLong() ?: 0L
        }
        val agentUid = agentEntity.agentUid

        insertOrUpdateVerbLangMap(repo.xLangMapEntryDao, statement.verb!!, verbEntity,
                repo.languageDao, repo.languageVariantDao)

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
            val authorityEntity = getAgent(repo.agentDao, repo.personDao, authority!!)
            authorityUid = authorityEntity.agentUid
        }

        var xObjectEntity: XObjectEntity? = null
        val xObjectVal = statement.`object`
        if (xObjectVal != null) {
            xObjectEntity = insertOrUpdateXObject(repo.xObjectDao, xObjectVal, gson,
                    repo.contentEntryDao, contentEntryUid)

            insertOrUpdateXObjectLangMap(repo.xLangMapEntryDao, xObjectVal, xObjectEntity,
                    repo.languageDao, repo.languageVariantDao)
        }

        var subActorUid: Long = 0
        var subVerbUid: Long = 0
        var subObjectUid: Long = 0

        if (statement.subStatement != null) {
            val subStatement = statement.subStatement

            val subAgent = getAgent(repo.agentDao, repo.personDao, subStatement!!.actor!!)
            subActorUid = subAgent.agentUid

            val subVerb = insertOrUpdateVerb(repo.verbDao, subStatement.verb!!)
            subVerbUid = subVerb.verbUid

            insertOrUpdateVerbLangMap(repo.xLangMapEntryDao, subStatement.verb!!, subVerb,
                    repo.languageDao, repo.languageVariantDao)

            val subObject = insertOrUpdateXObject(repo.xObjectDao, subStatement.`object`!!, gson,
                    repo.contentEntryDao, contentEntryUid)

            subObjectUid = subObject.xObjectUid

            insertOrUpdateXObjectLangMap(repo.xLangMapEntryDao, subStatement.`object`!!, subObject,
                    repo.languageDao, repo.languageVariantDao)

        }

        var contextStatementId = ""
        var instructorUid: Long = 0
        var teamUid: Long = 0

        val statementContext = statement.context
        if (statementContext != null) {

            val contextInstructor = statementContext.instructor
            if (contextInstructor != null) {
                val instructorAgent = getAgent(repo.agentDao, repo.personDao, contextInstructor)
                instructorUid = instructorAgent.agentUid
            }

            val contextTeam = statementContext.team
            if (contextTeam != null) {
                val teamAgent = getAgent(repo.agentDao, repo.personDao, contextTeam)
                teamUid = teamAgent.agentUid
            }

            contextStatementId = statementContext.statement?.id ?: ""
        }

        val entry = db.contentEntryDao.findByUid(contentEntryUid)
        val contentEntryRoot = xObjectEntity?.objectId == entry?.entryId

        val statementEntity = insertOrUpdateStatementEntity(repo.statementDao, statement, gson,
                person?.personUid ?: 0,
                verbEntity.verbUid,
                xObjectEntity?.xObjectUid ?: 0,
                contextStatementId, instructorUid,
                agentUid, authorityUid, teamUid,
                subActorUid, subVerbUid, subObjectUid,
                contentEntryUid = contentEntryUid,
                learnerGroupUid = learnerGroupUid, contentEntryRoot = contentEntryRoot,
                clazzUid = clazzUid)

        //ContentEntry should be available locally
        if (contentEntryRoot) {
            XapiUtil.insertOrUpdateEntryProgress(statementEntity, repo,
                    verbEntity)
        }

        val contextActivities = statement.context?.contextActivities
        if (contextActivities != null) {
            contextActivities.parent?.also {
                createAllContextActivities(it, statementEntity.statementUid,
                        ContextXObjectStatementJoinDaoCommon.CONTEXT_FLAG_PARENT)
            }

            contextActivities.category?.also {
                createAllContextActivities(it,
                        statementEntity.statementUid, ContextXObjectStatementJoinDaoCommon.CONTEXT_FLAG_CATEGORY)
            }

            contextActivities.grouping?.also {
                createAllContextActivities(it,
                        statementEntity.statementUid, ContextXObjectStatementJoinDaoCommon.CONTEXT_FLAG_GROUPING)
            }

            contextActivities.other?.also {
                createAllContextActivities(it,
                        statementEntity.statementUid, ContextXObjectStatementJoinDaoCommon.CONTEXT_FLAG_OTHER)
            }
        }
        return statementEntity
    }

    fun createAllContextActivities(list: List<XObject>?, statementUid: Long, flag: Int) {
        list?.filter { it.id != null  }?.forEach { xObject ->
            val xobjectEntity = insertOrUpdateXObject(repo.xObjectDao, xObject, gson, repo.contentEntryDao)
            insertOrUpdateContextStatementJoin(repo.contextXObjectStatementJoinDao,
                    statementUid, xobjectEntity.xObjectUid, flag)
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
        val statementList = db.statementDao.findByStatementIdList(ids as List<String>)

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
