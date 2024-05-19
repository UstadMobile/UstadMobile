package com.ustadmobile.core.domain.xapi.model

import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.xapiRequireDurationOrNullAsLong
import com.ustadmobile.core.domain.xapi.xapiRequireNotNullOrThrow
import com.ustadmobile.core.domain.xapi.xapiRequireTimestampAsLong
import com.ustadmobile.core.domain.xapi.xapiRequireValidUuidOrNull
import com.ustadmobile.core.domain.xxhash.XXHasher
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.AgentEntity
import com.ustadmobile.lib.db.entities.StatementEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

const val XAPI_RESULT_EXTENSION_PROGRESS = "https://w3id.org/xapi/cmi5/result/extensions/progress"

@Serializable
data class Statement(
    val id: String? = null,
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
    val attachments: List<Attachment>? = null,
    val objectType: String? = null,
)

data class StatementEntities(
    val statementEntity: StatementEntity,
    val agentEntity: AgentEntity?,
    val verbEntities: VerbEntities,
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
    val statementUuid = id?.let { uuidFrom(it) } ?: throw IllegalArgumentException("id is null")
    val contextRegistration = xapiRequireValidUuidOrNull(
        context?.registration, errorMessage = "Invalid context registration uuid"
    )

    return StatementEntities(
        statementEntity = StatementEntity(
            statementIdHi = statementUuid.mostSignificantBits,
            statementIdLo = statementUuid.leastSignificantBits,
            statementActorPersonUid = if(
                actor?.account?.homePage == xapiSession.endpoint.url &&
                actor.account.name == xapiSession.accountUsername
            ) {
                xapiSession.accountPersonUid
            }else {
                0
            },
            statementVerbUid = xxHasher.hash(
                xapiRequireNotNullOrThrow(verb?.id,"Missing verb id")
            ),
            resultCompletion = result?.completion ?: false,
            resultSuccess = result?.success,
            resultScoreScaled = result?.score?.scaled,
            resultScoreRaw = result?.score?.raw,
            resultScoreMin = result?.score?.min,
            resultScoreMax = result?.score?.max,
            resultDuration = xapiRequireDurationOrNullAsLong(result?.duration),
            resultResponse = result?.response,
            timestamp = timestamp?.let { xapiRequireTimestampAsLong(it) } ?: systemTimeInMillis(),
            stored = systemTimeInMillis(),
            contextRegistrationHi = contextRegistration?.mostSignificantBits ?: 0,
            contextRegistrationLo = contextRegistration?.leastSignificantBits ?: 0,
            contextPlatform = context?.platform,
            statementContentEntryUid = xapiSession.contentEntryUid,
            statementClazzUid = xapiSession.clazzUid,
            statementCbUid = xapiSession.cbUid,
            contentEntryRoot = `object`?.id != null && `object`.id == xapiSession.rootActivityId,
            fullStatement = exactJson,
            extensionProgress = result?.extensions?.get(XAPI_RESULT_EXTENSION_PROGRESS)
                ?.jsonPrimitive?.intOrNull
        ),
        agentEntity = if(actor?.isAgent() == true) {
            actor.toAgentEntity(xxHasher)
        }else {
            null
        },
        verbEntities = xapiRequireNotNullOrThrow(
            verb, message = "Missing verb"
        ).toVerbEntities(xxHasher),
    )
}

