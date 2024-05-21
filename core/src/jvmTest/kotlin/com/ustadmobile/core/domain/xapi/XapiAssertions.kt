package com.ustadmobile.core.domain.xapi

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.model.XapiActor
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xapi.model.toActorEntity
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

/**
 * Verifies that the statement and all related entities are stored / parsed in the database
 * e.g. statement is retrieved by UUID, properties are parsed (e.g. duration), and related entities
 * are present (e.g. Verb, Agent, etc)
 */
fun assertStatementStoredInDb(
    statement: XapiStatement,
    db: UmAppDatabase,
    xxHasher: XXStringHasher,
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
            assertEquals(statement.`object`.id == xapiSession.rootActivityId,
                statementInDb.contentEntryRoot)
        }

        assertActorStoredInDb(statement.actor, db, xxHasher)
    }
}

fun assertActorStoredInDb(
    actor: XapiActor,
    db: UmAppDatabase,
    xxHasher: XXStringHasher,
) = runBlocking {
    if(actor is XapiAgent) {
        val agentEntity = actor.toActorEntity(xxHasher)
        val agentInDb = db.actorDao.findByUidAsync(agentEntity.actorUid)
        assertNotNull(agentInDb, "Agent is in database")
        assertEquals(actor.account?.name ?: "", agentInDb.actorAccountName ?: "")
        assertEquals(actor.mbox, agentInDb.actorMbox)
        assertEquals(actor.openid, agentInDb.actorOpenid)
    }
}
