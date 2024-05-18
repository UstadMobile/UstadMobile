package com.ustadmobile.core.domain.xapi.model

import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xxhash.XXHasher
import com.ustadmobile.lib.db.entities.AgentEntity
import com.ustadmobile.lib.db.entities.StatementEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Statement(
    val actor: Actor? = null,
    val verb: Verb? = null,
    @SerialName("object")
    val `object`: XObject? = null,
    val subStatement: Statement? = null,
    val result: Result? = null,
    val context: XContext? = null,
    val timestamp: String? = null,
    val stored: String? = null,
    val authority: Actor? = null,
    val version: String? = null,
    val id: String? = null,
    val attachments: List<Attachment>? = null,
    val objectType: String? = null,
)

data class StatementEntities(
    val statementEntity: StatementEntity,
    val agentEntity: AgentEntity?,
)

/**
 * Convert xAPI Statement JSON instead entities that can be stored in the database.
 *
 * Most of the time the statement received will be when running an xAPI activity, and the actor will
 * be an Agent for the current user.
 */
fun Statement.toEntities(
    xxHasher: XXHasher,
    xapiSession: XapiSession,
    exactJson: String,
): StatementEntities {
    val uuid = id?.let { uuidFrom(it) } ?: throw IllegalArgumentException("id is null")

    return StatementEntities(
        statementEntity = StatementEntity(
            statementIdHi = uuid.mostSignificantBits,
            statementIdLo = uuid.leastSignificantBits,
            statementActorPersonUid = if(
                actor?.account?.homePage == xapiSession.endpoint.url &&
                actor.account.name == xapiSession.accountUsername
            ) {
                xapiSession.accountPersonUid
            }else {
                0
            },
            fullStatement = exactJson,
        ),
        agentEntity = if(actor?.isAgent() == true) {
            actor.toAgentEntity(xxHasher)
        }else {
            null
        }
    )
}

