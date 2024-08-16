package com.ustadmobile.core.domain.xapi

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.xapi.StatementContextActivityJoin
import com.ustadmobile.core.domain.xapi.model.XapiActivity
import com.ustadmobile.core.domain.xapi.model.XapiActivityStatementObject
import com.ustadmobile.core.domain.xapi.model.XapiActor
import com.ustadmobile.core.domain.xapi.model.XapiVerb
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.XapiGroup
import com.ustadmobile.core.domain.xapi.model.XapiInteractionType
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xapi.model.XapiStatementRef
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.lib.db.entities.xapi.ActivityInteractionEntity
import com.ustadmobile.lib.db.entities.xapi.ActivityLangMapEntry
import com.ustadmobile.lib.db.entities.xapi.ActivityLangMapEntry.Companion.PROPNAME_DESCRIPTION
import com.ustadmobile.lib.db.entities.xapi.ActivityLangMapEntry.Companion.PROPNAME_NAME
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Verifies that the statement and all related entities are stored / parsed in the database
 * e.g. statement is retrieved by UUID, properties are parsed (e.g. duration), and related entities
 * are present (e.g. Verb, Agent, etc)
 *
 * @param subStatementUuid a substatement doesn't really have an ID of its own, our convention is
 * that we assign a substatement a UUID of the parent statement plus 1 on the least significant bits.
 */
fun assertStatementStoredInDb(
    statement: XapiStatement,
    db: UmAppDatabase,
    xxHasher: XXStringHasher,
    json: Json,
    xapiSession: XapiSessionEntity? = null,
    subStatementUuid: Uuid? = null
) {
    runBlocking {
        val stmtUuid = subStatementUuid ?: uuidFrom(statement.id!!)
        val statementInDb = db.statementDao().findById(
            statementIdHi = stmtUuid.mostSignificantBits,
            statementIdLo = stmtUuid.leastSignificantBits,
        )

        assertNotNull(statementInDb, "Statement $stmtUuid is in database")

        //Note nullable boolean properties need to use .toString() otherwise null can cause exceptions
        assertEquals(statement.result?.completion.toString(),
            statementInDb.resultCompletion.toString())
        assertEquals(statement.result?.success, statementInDb.resultSuccess)
        assertEquals(statement.result?.score?.scaled.toString(),
            statementInDb.resultScoreScaled.toString())
        assertEquals(statement.result?.score?.raw.toString(), statementInDb.resultScoreRaw.toString())
        assertEquals(statement.result?.score?.max.toString(), statementInDb.resultScoreMax.toString())
        assertEquals(statement.result?.score?.min.toString(), statementInDb.resultScoreMin.toString())
        assertEquals(xapiRequireDurationOrNullAsLong(statement.result?.duration),
            statementInDb.resultDuration)
        statement.timestamp?.also { timestamp ->
            assertEquals(xapiRequireTimestampAsLong(timestamp), statementInDb.timestamp)
        }
        assertNotEquals(0, statementInDb.stored)
        statement.context?.registration?.also { registrationUuid ->
            assertEquals(uuidFrom(registrationUuid),
                Uuid(statementInDb.contextRegistrationHi, statementInDb.contextRegistrationLo))
        }

        fun assertContextActivitiesInternal(
            contextActivities: List<XapiActivityStatementObject>?,
            typeFlag: Int
        ) =assertStatementContextActivitiesInDb(
            statement, contextActivities, db, typeFlag, xxHasher, json
        )
        assertContextActivitiesInternal(statement.context?.contextActivities?.parent,
            StatementContextActivityJoin.TYPE_PARENT)
        assertContextActivitiesInternal(statement.context?.contextActivities?.category,
            StatementContextActivityJoin.TYPE_CATEGORY)
        assertContextActivitiesInternal(statement.context?.contextActivities?.grouping,
            StatementContextActivityJoin.TYPE_GROUPING)
        assertContextActivitiesInternal(statement.context?.contextActivities?.other,
            StatementContextActivityJoin.TYPE_OTHER)

        if(xapiSession != null) {
            assertEquals(xapiSession.xseContentEntryUid, statementInDb.statementContentEntryUid)
            assertEquals(xapiSession.xseClazzUid, statementInDb.statementClazzUid)
            assertEquals(xapiSession.xseCbUid, statementInDb.statementCbUid)
            assertEquals(
                (statement.`object` as? XapiActivityStatementObject)?.id == xapiSession.xseRootActivityId,
                statementInDb.completionOrProgress)
        }

        assertActorStoredInDb(
            actor = statement.actor,
            actorUid = statementInDb.statementActorUid,
            db = db,
            xxHasher = xxHasher
        )

        assertVerbStoredInDb(
            verb = statement.verb,
            verbUid = statementInDb.statementVerbUid,
            db = db,
            xxHasher = xxHasher,
        )

        val stmtObject = statement.`object`
        when(stmtObject) {
            is XapiActivityStatementObject -> {
                assertActivityStoredInDb(
                    activity = stmtObject.definition,
                    activityUid = statementInDb.statementObjectUid1,
                    activityId = stmtObject.id,
                    db = db,
                    xxHasher = xxHasher,
                    json = json
                )
            }
            is XapiActor -> {
                assertActorStoredInDb(
                    actor = stmtObject,
                    actorUid = statementInDb.statementObjectUid1,
                    db = db,
                    xxHasher = xxHasher
                )
            }
            is XapiStatementRef -> {
                assertEquals(
                    uuidFrom(stmtObject.id),
                    Uuid(statementInDb.statementObjectUid1, statementInDb.statementObjectUid2)
                )
            }
            is XapiStatement -> {
                assertStatementStoredInDb(
                    statement = stmtObject,
                    db = db,
                    xxHasher = xxHasher,
                    json = json,
                    xapiSession = xapiSession,
                    subStatementUuid = Uuid(
                        stmtUuid.mostSignificantBits,
                        stmtUuid.leastSignificantBits + 1
                    )
                )
            }
            else -> {
                //do nothing
            }
        }
    }
}

fun assertStatementContextActivitiesInDb(
    statement: XapiStatement,
    contextActivities: List<XapiActivityStatementObject>?,
    db: UmAppDatabase,
    scajContextType: Int,
    stringHasher: XXStringHasher,
    json: Json,
) = runBlocking {
    if(contextActivities == null)
        return@runBlocking

    val stmtUuid = uuidFrom(statement.id!!)
    val contextActivityEntities = db.statementContextActivityJoinDao().findAllByStatementId(
        stmtUuid.mostSignificantBits, stmtUuid.leastSignificantBits, scajContextType
    )
    assertEquals(contextActivities.size, contextActivityEntities.size)
    contextActivities.forEach { contextActivity ->
        val contextActivityEntity = contextActivityEntities.firstOrNull {
            it.scajToActivityId == contextActivity.id
        }
        assertNotNull(contextActivityEntity)
        assertEquals(stringHasher.hash("$scajContextType-${contextActivity.id}"),
            contextActivityEntity.scajToHash)
        assertEquals(stringHasher.hash(contextActivity.id), contextActivityEntity.scajToActivityUid)

        assertActivityStoredInDb(
            activity = contextActivity.definition,
            activityUid = contextActivityEntity.scajToActivityUid,
            activityId = contextActivity.id,
            db = db,
            xxHasher = stringHasher,
            json = json,
        )
    }

}

fun assertActivityLangMapEntriesMatch(
    langMap: Map<String, String>?,
    langMapEntries: List<ActivityLangMapEntry>,
    propName: String,
    xxHasher: XXStringHasher,
) {
    langMap?.forEach { nameEntry ->
        val langMapEntity = langMapEntries.firstOrNull {
            it.almeHash == xxHasher.hash("$propName-${nameEntry.key}")
        }
        assertNotNull(langMapEntity)
        assertEquals(nameEntry.key, langMapEntity.almeLangCode)
        assertEquals(nameEntry.value, langMapEntity.almeValue)
    }
}

fun assertActivityStoredInDb(
    activity: XapiActivity?,
    activityUid: Long,
    activityId: String,
    db: UmAppDatabase,
    xxHasher: XXStringHasher,
    json: Json,
) = runBlocking {
    assertEquals(xxHasher.hash(activityId), activityUid)
    val langMapEntries = db.activityLangMapEntryDao()
        .findAllByActivityUid(activityUid)
    val interactionEntities = db.activityInteractionDao()
        .findAllByActivityUidAsync(activityUid)
    val extensionEntities = db.activityExtensionDao().findAllByActivityUid(activityUid)

    fun List<XapiActivity.Interaction>.assertInteractionsStoredInDb(propName: String, propFlag: Int) {
        forEach { interaction ->
            val interactionEntity = interactionEntities.firstOrNull {
                it.aieId == interaction.id
            }
            assertNotNull(interactionEntity)
            assertEquals(interaction.id, interactionEntity.aieId)
            assertEquals(propFlag, interactionEntity.aieProp)
            assertActivityLangMapEntriesMatch(
                interaction.description, langMapEntries, "$propName-${interaction.id}", xxHasher)
            assertTrue { langMapEntries.any { it.almeAieHash == interactionEntity.aieHash } }
        }
    }

    val activityInDb = db.activityEntityDao().findByUidAsync(activityUid)
    assertNotNull(activityInDb, "Activity $activityUid is in database")
    assertEquals(activityInDb.actIdIri, activityId)
    val responsePatternsFromDb = activityInDb.actCorrectResponsePatterns?.let {
        json.decodeFromString(ListSerializer(String.serializer()), it)
    } ?: emptyList()

    activity?.correctResponsePattern?.forEach {
        assertTrue(it in responsePatternsFromDb)
    }
    assertEquals(activity?.interactionType,
        XapiInteractionType.fromDbFlag(activityInDb.actInteractionType))

    activity?.choices?.assertInteractionsStoredInDb("choices", ActivityInteractionEntity.PROP_CHOICES)
    activity?.scale?.assertInteractionsStoredInDb("scale", ActivityInteractionEntity.PROP_SCALE)
    activity?.source?.assertInteractionsStoredInDb("source", ActivityInteractionEntity.PROP_SOURCE)
    activity?.target?.assertInteractionsStoredInDb("target", ActivityInteractionEntity.PROP_TARGET)
    activity?.steps?.assertInteractionsStoredInDb("steps", ActivityInteractionEntity.PROP_STEPS)

    assertActivityLangMapEntriesMatch(
        activity?.name, langMapEntries, PROPNAME_NAME, xxHasher
    )
    assertActivityLangMapEntriesMatch(
        activity?.description, langMapEntries, PROPNAME_DESCRIPTION, xxHasher
    )

    activity?.extensions?.forEach { extension ->
        val extensionEntity = extensionEntities.firstOrNull {
            it.aeeKey == extension.key
        }

        assertNotNull(extensionEntity)
        val extensionJsonDecoded = extensionEntity.aeeJson?.let {
            json.decodeFromString(JsonElement.serializer(), it)
        }
        assertEquals(extension.value, extensionJsonDecoded)
    }
}

fun assertVerbStoredInDb(
    verb: XapiVerb,
    verbUid: Long,
    db: UmAppDatabase,
    xxHasher: XXStringHasher,
) = runBlocking {
    val verbInDb = db.verbDao().findByUid(verbUid)
    assertNotNull(verbInDb, "Verb $verbUid is in database")
    assertEquals(verb.id, verbInDb.verbUrlId)

    val verbLangMapEntries = db.verbLangMapEntryDao().findByVerbUidAsync(verbUid)
    verb.display?.forEach { displayEntry ->
        val langMapEntity = verbLangMapEntries.firstOrNull {
            it.vlmeLangCode == displayEntry.key
        }

        assertNotNull(langMapEntity,
            "Verb ${verb.id} should have entity for langmap lang code ${displayEntry.key}")
        assertEquals(xxHasher.hash(displayEntry.key), langMapEntity.vlmeLangHash)
        assertEquals(displayEntry.value, langMapEntity.vlmeEntryString)
    }
}

/**
 * @param actorUid Normally this is the identifier hash, however in the case of an anonymous group
 * it may be a generated key.
 */
fun assertActorStoredInDb(
    actor: XapiActor,
    actorUid: Long,
    db: UmAppDatabase,
    xxHasher: XXStringHasher,
) = runBlocking {
    val agentInDb = db.actorDao().findByUidAsync(actorUid)
    assertNotNull(agentInDb, "Agent is in database")

    if(actor is XapiAgent) {
        assertEquals(actor.account?.name ?: "", agentInDb.actorAccountName ?: "")
        assertEquals(actor.account?.homePage ?: "", agentInDb.actorAccountHomePage ?: "")
        assertEquals(actor.mbox, agentInDb.actorMbox)
        assertEquals(actor.openid, agentInDb.actorOpenid)
    }else if(actor is XapiGroup) {
        val membersInDb = db.actorDao().findGroupMembers(actorUid)
        actor.member.forEach { member ->
            val memberInDb = membersInDb.firstOrNull { it.actorUid == member.identifierHash(xxHasher) }
            assertNotNull(memberInDb, "Member $member was found in db when querying for group members")
        }
    }
}
