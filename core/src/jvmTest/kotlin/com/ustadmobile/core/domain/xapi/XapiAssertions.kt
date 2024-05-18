package com.ustadmobile.core.domain.xapi

import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.model.Actor
import com.ustadmobile.core.domain.xapi.model.Statement
import com.ustadmobile.core.domain.xapi.model.toAgentEntity
import com.ustadmobile.core.domain.xxhash.XXHasher
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Verifies that the statement and all related entities are stored / parsed in the database
 * e.g. statement is retrieved by UUID, properties are parsed (e.g. duration), and related entities
 * are present (e.g. Verb, Agent, etc)
 */
fun assertStatementStoredInDb(
    statement: Statement,
    db: UmAppDatabase,
    xxHasher: XXHasher,
) {
    runBlocking {
        val stmtUuid = uuidFrom(statement.id!!)
        val statementInDb = db.statementDao.findById(
            statementIdHi = stmtUuid.mostSignificantBits,
            statementIdLo = stmtUuid.leastSignificantBits,
        )

        assertNotNull(statementInDb, "Statement $stmtUuid is in database")

        assertActorStoredInDb(statement.actor!!, db, xxHasher)
    }
}

fun assertActorStoredInDb(
    actor: Actor,
    db: UmAppDatabase,
    xxHasher: XXHasher,
) = runBlocking {
    if(actor.isAgent()) {
        val agentEntity = actor.toAgentEntity(xxHasher)
        val agentInDb = db.agentDao.findByUidAsync(agentEntity.agentUid)
        assertNotNull(agentInDb, "Agent is in database")
        assertEquals(actor.account?.name ?: "", agentInDb.agentAccountName ?: "")
        assertEquals(actor.mbox, agentInDb.agentMbox)
        assertEquals(actor.openid, agentInDb.agentOpenid)
    }
}
