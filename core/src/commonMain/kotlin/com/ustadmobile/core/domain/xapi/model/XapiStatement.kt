package com.ustadmobile.core.domain.xapi.model

import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.domain.xapi.XapiException
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.ext.resultProgressExtension
import com.ustadmobile.core.domain.xapi.xapiRequireDurationOrNullAsLong
import com.ustadmobile.core.domain.xapi.xapiRequireTimestampAsLong
import com.ustadmobile.core.domain.xapi.xapiRequireValidIRI
import com.ustadmobile.core.domain.xapi.xapiRequireValidUuidOrNull
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.util.ext.toEmptyIfNull
import com.ustadmobile.door.DoorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import com.ustadmobile.lib.db.entities.xapi.StatementEntityJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val XAPI_RESULT_EXTENSION_PROGRESS = "https://w3id.org/xapi/cmi5/result/extensions/progress"

/**
 * XapiStatement represents both a Statement and a SubStatement, therefor it implements the sealed
 * interface XapiStatementObject
 */
@Serializable
data class XapiStatement(
    val id: String? = null,
    val actor: XapiActor,
    val verb: XapiVerb,
    @SerialName("object")
    val `object`: XapiStatementObject,
    val result: XapiResult? = null,
    val context: XapiContext? = null,
    val timestamp: String? = null,
    val stored: String? = null,
    val authority: XapiActor? = null,
    val version: String? = null,
    val attachments: List<Attachment>? = null,
    override val objectType: XapiObjectType? = null,
): XapiStatementObject

data class StatementEntities(
    val statementEntity: StatementEntity? = null,
    val statementEntityJson: StatementEntityJson? = null,
    val actorEntities: List<ActorEntities>? = null,
    val verbEntities: VerbEntities? = null,
    val activityEntities: List<ActivityEntities>? = null,
)

/**
 * Convert xAPI Statement JSON instead entities that can be stored in the database.
 *
 * Most of the time the statement received will be when running an xAPI activity, and the actor will
 * be an Agent for the current user.
 *
 * @param isSubStatement true if the statement being processed is a substatement, in which case, it
 * must not as per the spec contain another nested substatement.
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
    if(isSubStatement && `object` is XapiStatement)
        throw XapiException(400, "SubStatement cannot have another nested subs== XapiObjectType.SubStatementtatement")

    val contextRegistration = xapiRequireValidUuidOrNull(
        context?.registration, errorMessage = "Invalid context registration uuid"
    )

    val statementActorEntities = actor.toEntities(
        stringHasher, primaryKeyManager, hasherFactory, xapiSession.knownActorUidToPersonUidMap
    )

    val authorityActor = authority?.toEntities(
        stringHasher, primaryKeyManager, hasherFactory, xapiSession.knownActorUidToPersonUidMap
    )

    val contextInstructorActorEntities = context?.instructor?.toEntities(
        stringHasher, primaryKeyManager, hasherFactory,
        xapiSession.knownActorUidToPersonUidMap
    )

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
                authorityActorUid = authorityActor?.actor?.actorUid ?: 0,
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
                contextInstructorActorUid = contextInstructorActorEntities?.actor?.actorUid ?: 0,
                statementContentEntryUid = xapiSession.contentEntryUid,
                statementClazzUid = xapiSession.clazzUid,
                statementCbUid = xapiSession.cbUid,
                contentEntryRoot = (`object` as? XapiActivityStatementObject)?.id == xapiSession.rootActivityId,
                extensionProgress = resultProgressExtension,
                statementObjectType = `object`.objectTypeFlag,
                statementObjectUid1 = statementObjectForeignKeys.first,
                statementObjectUid2 = statementObjectForeignKeys.second,
                isSubStatement = isSubStatement,
            ),
            statementEntityJson = StatementEntityJson(
                stmtJsonIdHi = statementUuid.mostSignificantBits,
                stmtJsonIdLo = statementUuid.leastSignificantBits,
                fullStatement = exactJson,
            ),
            actorEntities = buildList {
                add(statementActorEntities)
                contextInstructorActorEntities?.also { add(it) }
            },
            verbEntities = verb.toVerbEntities(stringHasher),
            /*
             * Note: object.objectToEntities will generate the ActivityEntities where an the object
             * of the statement is an activity.
             */
            activityEntities = context?.contextActivities
                ?.toEntities(stringHasher, json , statementUuid).toEmptyIfNull()
        ),
    ) + `object`.objectToEntities(
        stringHasher = stringHasher,
        primaryKeyManager = primaryKeyManager,
        hasherFactory = hasherFactory,
        json = json,
        xapiSession = xapiSession,
        parentStatementUuid = statementUuid,
    )

}

