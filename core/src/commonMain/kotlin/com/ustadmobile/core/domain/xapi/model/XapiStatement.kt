package com.ustadmobile.core.domain.xapi.model

import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.xapiRequireDurationOrNullAsLong
import com.ustadmobile.core.domain.xapi.xapiRequireNotNullOrThrow
import com.ustadmobile.core.domain.xapi.xapiRequireTimestampAsLong
import com.ustadmobile.core.domain.xapi.xapiRequireValidIRI
import com.ustadmobile.core.domain.xapi.xapiRequireValidUuidOrNull
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.door.DoorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

const val XAPI_RESULT_EXTENSION_PROGRESS = "https://w3id.org/xapi/cmi5/result/extensions/progress"

@Serializable
data class XapiStatement(
    val id: String? = null,
    val actor: XapiActor,
    val verb: Verb,
    @SerialName("object")
    val `object`: XapiStatementObject,
    val subStatement: XapiStatement? = null,
    val result: Result? = null,
    val context: XapiContext? = null,
    val timestamp: String? = null,
    val stored: String? = null,
    val authority: XapiActor? = null,
    val version: String? = null,
    val attachments: List<Attachment>? = null,
    val objectType: XapiObjectType? = null,
)

data class StatementEntities(
    val statementEntity: StatementEntity,
    val actorEntities: ActorEntities,
    val verbEntities: VerbEntities,
)

/**
 * Convert xAPI Statement JSON instead entities that can be stored in the database.
 *
 * Most of the time the statement received will be when running an xAPI activity, and the actor will
 * be an Agent for the current user.
 */
fun XapiStatement.toEntities(
    stringHasher: XXStringHasher,
    primaryKeyManager: DoorPrimaryKeyManager,
    hasherFactory: XXHasher64Factory,
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
                actor.account?.homePage == xapiSession.endpoint.url &&
                actor.account?.name == xapiSession.accountUsername
            ) {
                xapiSession.accountPersonUid
            }else {
                0
            },
            statementVerbUid = stringHasher.hash(
                xapiRequireValidIRI(verb.id,
                    "Statement $statementUuid VerbID ${verb.id} is not a valid IRI"
                )
            ),
            resultCompletion = result?.completion,
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
            contentEntryRoot = `object`.objectType.let { it == null || it == XapiObjectType.Activity } &&
                    `object`.id == xapiSession.rootActivityId,
            fullStatement = exactJson,
            extensionProgress = result?.extensions?.get(XAPI_RESULT_EXTENSION_PROGRESS)
                ?.jsonPrimitive?.intOrNull
        ),
        actorEntities = actor.toEntities(stringHasher, primaryKeyManager, hasherFactory),
        verbEntities = xapiRequireNotNullOrThrow(
            verb, message = "Missing verb"
        ).toVerbEntities(stringHasher),
    )
}

