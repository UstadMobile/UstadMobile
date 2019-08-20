package com.ustadmobile.port.sharedse.contentformats.xapi.endpoints

import com.google.gson.Gson
import com.neovisionaries.i18n.CountryCode
import com.neovisionaries.i18n.LanguageCode
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.UMTinCanUtil
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.sharedse.contentformats.xapi.*
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

    fun insertOrUpdateVerbLangMap(dao: XLangMapEntryDao, verb: Verb, verbEntity: VerbEntity, languageDao: LanguageDao, languageVariantDao: LanguageVariantDao) {

        val listToInsert = verb.display!!.map {

            val split = it.key.split("-")
            val lang = insertOrUpdateLanguageByTwoCode(languageDao, split[0])
            val variant = insertOrUpdateLanguageVariant(languageVariantDao, split[1], lang)

            XLangMapEntry(verbEntity.verbUid, 0, lang.langUid, variant?.langVariantUid
                    ?: 0, it.value)
        }
        dao.insertList(listToInsert)
    }

    fun insertOrUpdateXObjectLangMap(dao: XLangMapEntryDao, xobject: XObject, xObjectEntity: XObjectEntity, languageDao: LanguageDao, languageVariantDao: LanguageVariantDao) {

        val listToInsert = xobject.definition?.name?.map {

            val split = it.key.split("-")
            val lang = insertOrUpdateLanguageByTwoCode(languageDao, split[0])
            val variant = insertOrUpdateLanguageVariant(languageVariantDao, split[1], lang)

            XLangMapEntry(0, xObjectEntity.xObjectUid, lang.langUid, variant?.langVariantUid
                    ?: 0, it.value)
        }
        if (listToInsert != null) {
            dao.insertList(listToInsert)
        }
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

    fun insertOrUpdateXObject(dao: XObjectDao, xobject: XObject, gson: Gson, contentEntryDao: ContentEntryDao): XObjectEntity {

        val entity = dao.findByObjectId(xobject.id)

        var contentEntryUid = contentEntryDao.getContentEntryUidFromXapiObjectId(xobject.id!!)

        val definition = xobject.definition
        val changedXObject = XObjectEntity(xobject.id, xobject.objectType,
                if (definition != null) definition.type else "", if (definition != null) definition.interactionType else "",
                if (definition != null) gson.toJson(definition.correctResponsePattern) else "", contentEntryUid)

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

    /**
     * Given a language with 2 digit code, check if this language exists in db before adding it
     *
     * @param languageDao dao to query and insert
     * @param langTwoCode two digit code of language
     * @return the entity language
     */
    fun insertOrUpdateLanguageByTwoCode(languageDao: LanguageDao, langTwoCode: String): Language {

        var language = languageDao.findByTwoCode(langTwoCode)
        if (language == null) {
            language = Language()
            language.iso_639_1_standard = langTwoCode
            val nameOfLang = LanguageCode.getByCode(langTwoCode)
            if (nameOfLang != null) {
                language.name = nameOfLang.getName()
            }
            language.langUid = languageDao.insert(language)
        } else {
            val changedLang = Language()
            changedLang.langUid = language.langUid
            changedLang.iso_639_1_standard = langTwoCode
            val nameOfLang = LanguageCode.getByCode(langTwoCode)
            if (nameOfLang != null) {
                changedLang.name = nameOfLang.getName()
            }
            var isChanged = false
            if (language.iso_639_1_standard == null || language.iso_639_1_standard != changedLang.iso_639_1_standard) {
                isChanged = true
            }
            if (language.name == null || language.name == changedLang.name) {
                isChanged = true
            }
            if (isChanged) {
                languageDao.update(changedLang)
            }
            language = changedLang
        }
        return language
    }

    /**
     * Insert or updateState language variant
     *
     * @param variantDao variant dao to insert/updateState
     * @param variant    variant of the language
     * @param language   the language the variant belongs to
     * @return the language variant entry that was created/updated
     */
    fun insertOrUpdateLanguageVariant(variantDao: LanguageVariantDao, variant: String?, language: Language): LanguageVariant? {
        var languageVariant: LanguageVariant? = null
        if (variant != null && variant.isNotEmpty()) {
            var countryCode: CountryCode? = CountryCode.getByCode(variant)
            if (countryCode == null) {
                val countryList = CountryCode.findByName(variant)
                if (countryList.isNotEmpty()) {
                    countryCode = countryList[0]
                }
            }
            if (countryCode != null) {
                val alpha2 = countryCode.alpha2
                val name = countryCode.getName()
                languageVariant = variantDao.findByCode(alpha2)
                if (languageVariant == null) {
                    languageVariant = LanguageVariant()
                    languageVariant.countryCode = alpha2
                    languageVariant.name = name
                    languageVariant.langUid = language.langUid
                    languageVariant.langVariantUid = variantDao.insert(languageVariant)
                } else {
                    val changedVariant = LanguageVariant()
                    changedVariant.langVariantUid = languageVariant.langVariantUid
                    changedVariant.countryCode = alpha2
                    changedVariant.name = name
                    changedVariant.langUid = language.langUid
                    if (changedVariant != languageVariant) {
                        variantDao.update(languageVariant)
                    }
                    languageVariant = changedVariant
                }
            }
        }
        return languageVariant
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

        var statementEntity: StatementEntity? = dao.findByStatementId(statement.id!!)
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
            statementEntity.timestamp = UMCalendarUtil.parse8601Timestamp(statement.timestamp!!)
            statementEntity.stored = UMCalendarUtil.parse8601Timestamp(statement.stored!!)
            statementEntity.fullStatement = gson.toJson(statement)
            if (statement.result != null) {
                statementEntity.resultCompletion = statement.result!!.completion
                statementEntity.resultDuration = UMTinCanUtil.parse8601Duration(statement.result!!.duration!!)
                statementEntity.resultResponse = statement.result!!.response
                statementEntity.resultSuccess = statement.result!!.success.toInt().toByte()
                if (statement.result!!.score != null) {
                    statementEntity.resultScoreMax = statement.result!!.score!!.max
                    statementEntity.resultScoreMin = statement.result!!.score!!.min
                    statementEntity.resultScoreScaled = statement.result!!.score!!.scaled
                    statementEntity.resultScoreRaw = statement.result!!.score!!.raw
                }
            } else {
                statementEntity.resultSuccess = 0.toByte()
            }
            if (statement.context != null) {
                statementEntity.contextPlatform = statement.context!!.platform
                statementEntity.contextRegistration = statement.context!!.registration
            }
            statementEntity.statementUid = dao.insert(statementEntity)
        }
        return statementEntity
    }

    fun Boolean.toInt() = if (this) 1 else 0


}
