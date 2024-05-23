package com.ustadmobile.core.domain.xapi

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.model.XapiActivity
import com.ustadmobile.core.domain.xapi.model.XapiActivityStatementObject
import com.ustadmobile.core.domain.xapi.model.XapiActor
import com.ustadmobile.core.domain.xapi.model.XapiVerb
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.XapiGroup
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.lib.db.entities.xapi.ActivityLangMapEntry
import com.ustadmobile.lib.db.entities.xapi.ActivityLangMapEntry.Companion.PROPNAME_DESCRIPTION
import com.ustadmobile.lib.db.entities.xapi.ActivityLangMapEntry.Companion.PROPNAME_NAME
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Verifies that the statement and all related entities are stored / parsed in the database
 * e.g. statement is retrieved by UUID, properties are parsed (e.g. duration), and related entities
 * are present (e.g. Verb, Agent, etc)
 */
fun assertStatementStoredInDb(
    statement: XapiStatement,
    db: UmAppDatabase,
    xxHasher: XXStringHasher,
    json: Json,
    xapiSession: XapiSession? = null,
) {
    runBlocking {
        val stmtUuid = uuidFrom(statement.id!!)
        val statementInDb = db.statementDao.findById(
            statementIdHi = stmtUuid.mostSignificantBits,
            statementIdLo = stmtUuid.leastSignificantBits,
        )

        assertNotNull(statementInDb, "Statement $stmtUuid is in database")
        assertEquals(statement.result?.completion, statementInDb.resultCompletion)
        assertEquals(statement.result?.success, statementInDb.resultSuccess)
        assertEquals(statement.result?.score?.scaled, statementInDb.resultScoreScaled)
        assertEquals(statement.result?.score?.raw, statementInDb.resultScoreRaw)
        assertEquals(statement.result?.score?.max, statementInDb.resultScoreMax)
        assertEquals(statement.result?.score?.min, statementInDb.resultScoreMin)
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
        if(xapiSession != null) {
            assertEquals(xapiSession.contentEntryUid, statementInDb.statementContentEntryUid)
            assertEquals(xapiSession.clazzUid, statementInDb.statementClazzUid)
            assertEquals(xapiSession.cbUid, statementInDb.statementCbUid)
            assertEquals(
                (statement.`object` as? XapiActivityStatementObject)?.id == xapiSession.rootActivityId,
                statementInDb.contentEntryRoot)
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
            else -> {
                //do nothing
            }
        }
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
    val activityInDb = db.activityEntityDao.findByUidAsync(activityUid)
    assertNotNull(activityInDb, "Activity $activityUid is in database")
    assertEquals(activityInDb.actIdIri, activityId)
    val responsePatternsFromDb = activityInDb.actCorrectResponsePatterns?.let {
        json.decodeFromString(ListSerializer(String.serializer()), it)
    } ?: emptyList()

    activity?.correctResponsePattern?.forEach {
        assertTrue(it in responsePatternsFromDb)
    }

    val langMapEntries = db.activityLangMapEntryDao
        .findAllByActivityUid(activityUid)
    assertActivityLangMapEntriesMatch(
        activity?.name, langMapEntries, PROPNAME_NAME, xxHasher
    )
    assertActivityLangMapEntriesMatch(
        activity?.description, langMapEntries, PROPNAME_DESCRIPTION, xxHasher
    )

}

fun assertVerbStoredInDb(
    verb: XapiVerb,
    verbUid: Long,
    db: UmAppDatabase,
    xxHasher: XXStringHasher,
) = runBlocking {
    val verbInDb = db.verbDao.findByUid(verbUid)
    assertNotNull(verbInDb, "Verb $verbUid is in database")
    assertEquals(verb.id, verbInDb.verbUrlId)

    val verbLangMapEntries = db.verbLangMapEntryDao.findByVerbUidAsync(verbUid)
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
    val agentInDb = db.actorDao.findByUidAsync(actorUid)
    assertNotNull(agentInDb, "Agent is in database")

    if(actor is XapiAgent) {
        assertEquals(actor.account?.name ?: "", agentInDb.actorAccountName ?: "")
        assertEquals(actor.account?.homePage ?: "", agentInDb.actorAccountHomePage ?: "")
        assertEquals(actor.mbox, agentInDb.actorMbox)
        assertEquals(actor.openid, agentInDb.actorOpenid)
    }else if(actor is XapiGroup) {
        val membersInDb = db.actorDao.findGroupMembers(actorUid)
        actor.member.forEach { member ->
            val memberInDb = membersInDb.firstOrNull { it.actorUid == member.identifierHash(xxHasher) }
            assertNotNull(memberInDb, "Member $member was found in db when querying for group members")
        }
    }
}
