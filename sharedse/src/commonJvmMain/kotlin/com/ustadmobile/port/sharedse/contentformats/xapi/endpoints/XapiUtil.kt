package com.ustadmobile.port.sharedse.contentformats.xapi.endpoints

import com.google.gson.Gson
import com.neovisionaries.i18n.CountryCode
import com.neovisionaries.i18n.LanguageCode
import com.ustadmobile.core.contentformats.xapi.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.*
import java.util.*
import com.ustadmobile.core.util.parse8601Duration
import com.ustadmobile.lib.util.getSystemTimeInMillis

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
        val verbDisplay = verb.display
        if (verbDisplay != null) {
            val listToInsert = verbDisplay.mapNotNull {
                val lang = insertOrUpdateLanguageByTwoCode(languageDao, it.key.substringBefore('-'))
                val variant = it.key.substringAfter("-", "").let {
                    if(it != "")
                        insertOrUpdateLanguageVariant(languageVariantDao, it, lang)
                    else
                        null
                }

                val existingMap = dao.getXLangMapFromVerb(verbEntity.verbUid, lang.langUid)

                if(existingMap == null){
                    XLangMapEntry(verbEntity.verbUid, 0, lang.langUid,
                        variant?.langVariantUid ?: 0, it.value)
                }else{
                    null
                }
            }

            if (listToInsert != null && listToInsert.isNotEmpty()) {
                dao.insertList(listToInsert)
            }
        }
    }

    fun insertOrUpdateXObjectLangMap(dao: XLangMapEntryDao, xobject: XObject, xObjectEntity: XObjectEntity, languageDao: LanguageDao, languageVariantDao: LanguageVariantDao) {

        val listToInsert = xobject.definition?.name?.map {

            val split = it.key.split("-")
            val lang = insertOrUpdateLanguageByTwoCode(languageDao, split[0])
            val variant = insertOrUpdateLanguageVariant(languageVariantDao, split[1], lang)

            val existingMap = dao.getXLangMapFromObject(xObjectEntity.xObjectUid, lang.langUid)

            if(existingMap == null){
                XLangMapEntry(0, xObjectEntity.xObjectUid, lang.langUid, variant?.langVariantUid
                        ?: 0, it.value)
            }else{
                null
            }
        }?.filterNotNull()
        if (listToInsert != null && listToInsert.isNotEmpty()) {
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

    fun insertOrUpdateXObject(dao: XObjectDao, xobject: XObject, gson: Gson,
                              contentEntryDao: ContentEntryDao,
                              contentEntryUid: Long = 0L): XObjectEntity {
        val xObjectId = xobject.id ?: throw IllegalArgumentException("XObject has no id")
        val entity = dao.findByObjectId(xObjectId)

        val contentEntryUidVal = if (contentEntryUid != 0L) {
            contentEntryUid
        } else {
            contentEntryDao.getContentEntryUidFromXapiObjectId(xObjectId)
        }

        val definition = xobject.definition
        val changedXObject = XObjectEntity(xobject.id, xobject.objectType,
                if (definition != null) definition.type else "", if (definition != null) definition.interactionType else "",
                if (definition != null) gson.toJson(definition.correctResponsePattern) else "", contentEntryUidVal, xobject.statementRefUid)

        if (entity == null) {
            changedXObject.xObjectUid = dao.insert(changedXObject)
        } else {
            changedXObject.xObjectUid = entity.xObjectUid
            dao.takeIf { changedXObject != entity }?.update(changedXObject)
        }
        return changedXObject
    }

    fun getPerson(dao: PersonDao, actor: Actor): Person? {
        var person: Person? = null
        if (actor.account != null) {
            person = dao.findByUsername(actor.account!!.name)
        }
        return person
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

        stateContentDao.setInActiveStateContentByKeyAndUid(false, stateEntity.stateUid,
            getSystemTimeInMillis())

        insertOrUpdateStateContent(stateContentDao, content, stateEntity)

    }

    fun insertOrUpdateStatementEntity(dao: StatementDao, statement: Statement, gson: Gson,
                                      personUid: Long, verbUid: Long, objectUid: Long,
                                      contextStatementUid: String,
                                      instructorUid: Long, agentUid: Long, authorityUid: Long, teamUid: Long,
                                      subActorUid: Long, subVerbUid: Long, subObjectUid: Long,
                                      contentEntryUid: Long = 0L,
                                      learnerGroupUid: Long, contentEntryRoot: Boolean = false
                                      ,clazzUid: Long = 0L): StatementEntity {

        val statementId = statement.id
                ?: throw IllegalArgumentException("Statement $statement to be stored has no id!")

        var statementEntity: StatementEntity? = dao.findByStatementId(statementId)
        if (statementEntity == null) {
            statementEntity = StatementEntity().also {
                it.statementPersonUid = personUid
                it.statementId = statement.id
                it.statementVerbUid = verbUid
                it.xObjectUid = objectUid
                it.agentUid = agentUid
                it.authorityUid = authorityUid
                it.instructorUid = instructorUid
                it.teamUid = teamUid
                it.contextStatementId = contextStatementUid
                it.subStatementActorUid = subActorUid
                it.substatementVerbUid = subVerbUid
                it.subStatementObjectUid = subObjectUid
                it.timestamp = UMCalendarUtil.parse8601TimestampOrDefault(statement.timestamp)
                it.stored = UMCalendarUtil.parse8601TimestampOrDefault(statement.stored)
                it.statementContentEntryUid = contentEntryUid
                it.statementLearnerGroupUid = learnerGroupUid
                it.contentEntryRoot = contentEntryRoot
                it.statementClazzUid = clazzUid
                it.fullStatement = gson.toJson(statement)
            }


            val statementResult = statement.result
            if (statementResult != null) {
                statementEntity.resultCompletion = statementResult.completion
                statementEntity.resultDuration = statementResult.duration?.let { parse8601Duration(it) } ?: 0L
                statementEntity.resultResponse = statementResult.response
                var success = statementResult.success
                statementEntity.resultSuccess = if(success != null) {
                    if(success) StatementEntity.RESULT_SUCCESS else StatementEntity.RESULT_FAILURE
                }else{
                    StatementEntity.RESULT_UNSET
                }

                val resultScore = statementResult.score
                if (resultScore != null) {
                    statementEntity.resultScoreMax = resultScore.max
                    statementEntity.resultScoreMin = resultScore.min
                    statementEntity.resultScoreScaled = resultScore.scaled
                    statementEntity.resultScoreRaw = resultScore.raw
                }

                val progressExtension = statementResult.extensions?.get(XapiStatementEndpointImpl.EXTENSION_PROGRESS)
                if (progressExtension != null) {
                    //As this is being parsed as JSON - any number is counted as Double type
                    statementEntity.extensionProgress = progressExtension.anyToInt()

                }
            } else {
                statementEntity.resultSuccess = 0.toByte()
            }

            val statementContext = statement.context
            if (statementContext != null) {
                statementEntity.contextPlatform = statementContext.platform
                statementEntity.contextRegistration = statementContext.registration
            }

            statementEntity.statementUid = dao.insert(statementEntity)
        }
        return statementEntity
    }

    fun insertOrUpdateEntryProgress(statementEntity: StatementEntity, repo: UmAppDatabase, verbEntity: VerbEntity) {
        val statusFlag = getStatusFlag(verbEntity.urlId)
        var progress  = statementEntity.extensionProgress
        if(progress == 0 &&
                (statusFlag == StatementEntity.CONTENT_COMPLETE ||
                        statusFlag == StatementEntity.CONTENT_PASSED ||
                        statementEntity.resultCompletion)){
            progress = 100
            repo.statementDao.updateProgress(statementEntity.statementUid, progress,
                getSystemTimeInMillis())
        }
    }


    private fun getStatusFlag(id: String?): Int {
        return statusFlagMap[id] ?: 0
    }

    private val statusFlagMap = mapOf(
            "http://adlnet.gov/expapi/verbs/completed"
                    to StatementEntity.CONTENT_COMPLETE,
            "http://adlnet.gov/expapi/verbs/passed"
                    to StatementEntity.CONTENT_PASSED,
            "http://adlnet.gov/expapi/verbs/failed"
                    to StatementEntity.CONTENT_FAILED,
            "https://w3id.org/xapi/adl/verbs/satisfied"
                    to StatementEntity.CONTENT_COMPLETE)


    fun Boolean.toInt() = if (this) 1 else 0

    /**
     * Small utiltiy function to handle when we're not 100% sure about what we're getting in a Json
     * block
     */
    private fun Any?.anyToInt() = when {
        this is Double -> this.toInt()
        this is Float -> this.toInt()
        this is Int -> this
        this is Long -> this.toInt()
        this is String -> this.toInt()
        else -> 0
    }


}
