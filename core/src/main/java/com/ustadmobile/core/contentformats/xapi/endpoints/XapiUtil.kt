package com.ustadmobile.core.contentformats.xapi.endpoints

import com.google.gson.Gson
import com.ustadmobile.core.contentformats.xapi.*
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.UMTinCanUtil
import com.ustadmobile.lib.db.entities.*
import java.util.*

object XapiUtil {

    fun getAgent(dao: AgentDao, personDao: PersonDao, actor: Actor): AgentEntity {
        val person = getPerson(personDao, actor)
        var agentEntity: AgentEntity? = dao.getAgentByAnyId(
                actor.openid,
                actor.mbox,
                if (actor.account != null) actor.account!!.name else null,
                if (actor.account != null) actor.account!!.homePage else null,
                actor.mbox_sha1sum)
        if (agentEntity == null) {
            agentEntity = AgentEntity()
            agentEntity.agentOpenid = actor.openid
            agentEntity.agentMbox = actor.mbox
            agentEntity.agentAccountName = if (actor.account != null) actor.account!!.name else null
            agentEntity.agentHomePage = if (actor.account != null) actor.account!!.homePage else null
            agentEntity.agentMbox_sha1sum = actor.mbox_sha1sum
            agentEntity.agentPersonUid = person?.personUid ?: 0
            agentEntity.agentUid = dao.insert(agentEntity)
        }

        return agentEntity
    }


    fun insertOrUpdateVerb(dao: VerbDao, verb: Verb): VerbEntity {

        var verbEntity: VerbEntity? = dao.findByUrl(verb.id)
        if (verbEntity == null) {
            verbEntity = VerbEntity()
            verbEntity.urlId = verb.id
            verbEntity.verbUid = dao.insert(verbEntity)
        }
        return verbEntity

    }

    fun insertOrUpdateContextStatementJoin(dao: ContextXObjectStatementJoinDao, statementUid: Long, objectUid: Long, flag: Int): ContextXObjectStatementJoin {

        var join: ContextXObjectStatementJoin? = dao.findByStatementAndObjectUid(statementUid, objectUid)
        if (join == null) {
            join = ContextXObjectStatementJoin()
            join.contextActivityFlag = flag
            join.contextStatementUid = statementUid
            join.contextXObjectUid = objectUid
            join.contextXObjectStatementJoinUid = dao.insert(join)
        }
        return join
    }

    fun getPerson(dao: PersonDao, actor: Actor): Person? {
        var person: Person? = null
        if (actor.account != null) {
            person = dao.findByUsername(actor.account!!.name)
        }
        return person
    }

    fun insertOrUpdateXObject(dao: XObjectDao, xobject: XObject, gson: Gson): XObjectEntity {

        val entity = dao.findByObjectId(xobject.id)

        val definition = xobject.definition
        val changedXObject = XObjectEntity(xobject.id, xobject.objectType,
                if (definition != null) definition.type else "", if (definition != null) definition.interactionType else "",
                if (definition != null) gson.toJson(definition.correctResponsePattern) else "")

        if (entity == null) {
            changedXObject.xObjectUid = dao.insert(changedXObject)
        } else {
            changedXObject.xObjectUid = entity.xObjectUid
            if (changedXObject != entity) {
                dao.update(changedXObject)
            }
        }
        return changedXObject
    }

    fun insertOrUpdateState(dao: StateDao, state: State, agentUid: Long): StateEntity {
        val stateEntity = dao.findByStateId(state.stateId, agentUid, state.activityId, state.registration)

        val changedState = StateEntity(state.activityId, agentUid,
                state.registration, state.stateId, true, System.currentTimeMillis())

        if (stateEntity == null) {
            changedState.stateUid = dao.insert(changedState)
        } else {
            changedState.stateUid = stateEntity.stateUid
            if (changedState != stateEntity) {
                dao.update(changedState)
            }
        }
        return changedState
    }

    fun insertOrUpdateStateContent(dao: StateContentDao, contentMap: HashMap<String, Any>, stateEntity: StateEntity) {

        for (key in contentMap.keys) {

            val value = contentMap[key]
            val content = dao.findStateContentByKeyAndStateUid(key, stateEntity.stateUid)
            if (content == null) {
                val contentEntity = StateContentEntity(key, stateEntity.stateUid, value.toString(), true)
                contentEntity.stateContentUid = dao.insert(contentEntity)
            } else {
                val changedContent = StateContentEntity(key, stateEntity.stateUid, value.toString(), true)
                changedContent.stateContentUid = content.stateContentUid
                if (changedContent != content) {
                    dao.update(changedContent)
                }
            }
        }
    }

    fun deleteAndInsertNewStateContent(stateContentDao: StateContentDao, content: HashMap<String, Any>, stateEntity: StateEntity) {

        stateContentDao.setInActiveStateContentByKeyAndUid(false, stateEntity.stateUid)

        insertOrUpdateStateContent(stateContentDao, content, stateEntity)

    }

    fun insertOrUpdateStatementEntity(dao: StatementDao, statement: Statement, gson: Gson,
                                      personUid: Long, verbUid: Long, objectUid: Long,
                                      contextStatementUid: String,
                                      instructorUid: Long, agentUid: Long, authorityUid: Long, teamUid: Long,
                                      subActorUid: Long, subVerbUid: Long, subObjectUid: Long): StatementEntity {

        var statementEntity: StatementEntity? = dao.findByStatementId(statement.id)
        if (statementEntity == null) {
            statementEntity = StatementEntity()
            statementEntity.personUid = personUid
            statementEntity.statementId = statement.id
            statementEntity.verbUid = verbUid
            statementEntity.xObjectUid = objectUid
            statementEntity.agentUid = agentUid
            statementEntity.authorityUid = authorityUid
            statementEntity.instructorUid = instructorUid
            statementEntity.teamUid = teamUid
            statementEntity.contextStatementId = contextStatementUid
            statementEntity.subStatementActorUid = subActorUid
            statementEntity.substatementVerbUid = subVerbUid
            statementEntity.subStatementObjectUid = subObjectUid
            statementEntity.timestamp = UMCalendarUtil.parse8601Timestamp(statement.timestamp!!).timeInMillis
            statementEntity.stored = UMCalendarUtil.parse8601Timestamp(statement.stored!!).timeInMillis
            statementEntity.fullStatement = gson.toJson(statement)
            if (statement.result != null) {
                statementEntity.isResultCompletion = statement.result!!.completion
                statementEntity.resultDuration = UMTinCanUtil.parse8601Duration(statement.result!!.duration!!)
                statementEntity.resultResponse = statement.result!!.response
                statementEntity.isResultSuccess = statement.result!!.success
                if (statement.result!!.score != null) {
                    statementEntity.resultScoreMax = statement.result!!.score!!.max
                    statementEntity.resultScoreMin = statement.result!!.score!!.min
                    statementEntity.resultScoreScaled = statement.result!!.score!!.scaled
                    statementEntity.resultScoreRaw = statement.result!!.score!!.raw
                }
            }
            if (statement.context != null) {
                statementEntity.contextPlatform = statement.context!!.platform
                statementEntity.contextRegistration = statement.context!!.registration
            }
            statementEntity.statementUid = dao.insert(statementEntity)
        }
        return statementEntity
    }


}
