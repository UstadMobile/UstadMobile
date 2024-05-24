package com.ustadmobile.core.domain.xapi.model

import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.db.dao.xapi.StatementContextActivityJoin
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.xapiRequireDurationOrNullAsLong
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

const val XAPI_RESULT_EXTENSION_PROGRESS = "https://w3id.org/xapi/cmi5/result/extensions/progress"

@Serializable
data class XapiStatement(
    val id: String? = null,
    val actor: XapiActor,
    val verb: XapiVerb,
    @SerialName("object")
    val `object`: XapiStatementObject,
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
    val statementEntity: StatementEntity? = null,
    val statementContextActivityJoins: List<StatementContextActivityJoin> = emptyList(),
    val actorEntities: ActorEntities? = null,
    val verbEntities: VerbEntities? = null,
    val activityEntities: List<ActivityEntities>? = null,
)

/**
 * Convert xAPI Statement JSON instead entities that can be stored in the database.
 *
 * Most of the time the statement received will be when running an xAPI activity, and the actor will
 * be an Agent for the current user.
 *
 * @return list of two statement entities - the statement itself, and the entities of the object (
 * which could be a substatement, agent, group, or statementref)
 */
fun XapiStatement.toEntities(
    stringHasher: XXStringHasher,
    primaryKeyManager: DoorPrimaryKeyManager,
    hasherFactory: XXHasher64Factory,
    json: Json,
    xapiSession: XapiSession,
    exactJson: String?,
    isSubStatement: Boolean = false,
): List<StatementEntities> {
    val statementUuid = id?.let { uuidFrom(it) } ?: throw IllegalArgumentException("id is null")
    val contextRegistration = xapiRequireValidUuidOrNull(
        context?.registration, errorMessage = "Invalid context registration uuid"
    )

    val statementActorEntities = actor.toEntities(stringHasher, primaryKeyManager,
        hasherFactory)
    val statementObjectForeignKeys = `object`.objectForeignKeys(stringHasher, statementUuid)

    return listOf(
        StatementEntities(
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
                statementActorUid = statementActorEntities.actor.actorUid,
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
                contentEntryRoot = (`object` as? XapiActivityStatementObject)?.id == xapiSession.rootActivityId,
                fullStatement = exactJson,
                extensionProgress = result?.extensions?.get(XAPI_RESULT_EXTENSION_PROGRESS)
                    ?.jsonPrimitive?.intOrNull,
                statementObjectType = `object`.objectTypeFlag,
                statementObjectUid1 = statementObjectForeignKeys.first,
                statementObjectUid2 = statementObjectForeignKeys.second,
            ),
            actorEntities = actor.toEntities(stringHasher, primaryKeyManager, hasherFactory),
            verbEntities = verb.toVerbEntities(stringHasher),
            activityEntities = context?.contextActivities
                ?.toEntities(stringHasher, json , statementUuid)
        ),
        `object`.objectToEntities(stringHasher, primaryKeyManager, hasherFactory, json)
    ).filterNotNull()

}

