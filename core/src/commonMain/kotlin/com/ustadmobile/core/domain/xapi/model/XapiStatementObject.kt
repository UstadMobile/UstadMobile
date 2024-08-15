package com.ustadmobile.core.domain.xapi.model

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.xapi.XapiException
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.door.DoorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.xapi.XapiEntityObjectTypeFlags
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * As per the xAPI spec, can be an Activity, Actor (Agent or Group), or a statement reference.
 *
 * Statement objects look like this:
 *   {
 *      id : "http://...",
 *      objectType: "Activity|Agent|Group|StatementRef"
 *      definition: {
 *         ... may be present if an Activity
 *      }
 *   }
 * So we need to have four different types of XapiStatementObject so the serializer can look at the
 * objectType property to determine the type of the definition object. This sealed interface
 * represents the valid types for the object property on an xAPI statement.
 *
 * When the objectType is Agent or Group, then there is no id or definition property, the XapiAgent
 * and XapiGroup entities are defined as implementing the sealed XapiStatementObject sealed
 * interface themselves.
 */
@Serializable(with = XapiStatementObjectSerializer::class)
sealed interface XapiStatementObject {
    val objectType: XapiObjectType?
}

@Serializable
data class XapiActivityStatementObject(
    override val objectType: XapiObjectType? = null,
    val id: String,
    val definition: XapiActivity? = null,
): XapiStatementObject

/**
 * As per the spec, if the objectType is not specified, it defaults to Activity.
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2441-when-the-objecttype-is-activity
 */
val XapiStatementObject.objectTypeFlag: Int
    get() = objectType?.typeFlag ?: XapiEntityObjectTypeFlags.ACTIVITY

fun XapiStatementObject.objectForeignKeys(
    stringHasher: XXStringHasher,
    statementUuid: Uuid,
): Pair<Long, Long> {
    return when(this) {
        is XapiActivityStatementObject -> {
            Pair(stringHasher.hash(id), 0)
        }
        is XapiAgent -> {
            Pair(this.identifierHash(stringHasher), 0)
        }
        is XapiGroup -> {
            Pair(this.identifierHash(stringHasher), 0)
        }
        is XapiStatementRef -> {
            val uuid = uuidFrom(id)
            Pair(uuid.mostSignificantBits, uuid.leastSignificantBits)
        }
        is XapiStatement -> {
            //As per the doc on StatementEntity itself, where there is a substatement, the
            //statement uid is the uid of the statement itself + 1.
            Pair(statementUuid.mostSignificantBits, statementUuid.leastSignificantBits + 1)
        }
    }
}


/**
 * Convert the statement object into entities. Because the object could be an activity, substatement,
 * statementref, agent, or group, this function returns StatementEntities itself
 */
fun XapiStatementObject.objectToEntities(
    stringHasher: XXStringHasher,
    primaryKeyManager: DoorPrimaryKeyManager,
    hasherFactory: XXHasher64Factory,
    json: Json,
    xapiSession: XapiSessionEntity,
    knownActorUidToPersonUidMap: Map<Long, Long>,
    parentStatementUuid: Uuid,
    learningSpace: LearningSpace,
) : List<StatementEntities> {
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    return when(this) {
        is XapiActivityStatementObject -> {
            listOf(
                StatementEntities(
                    activityEntities = listOf(definition.toEntities(id, stringHasher, json))
                )
            )
        }

        is XapiActor -> {
            listOf(
                StatementEntities(
                    actorEntities = listOf(
                        toEntities(
                            stringHasher, primaryKeyManager, hasherFactory, knownActorUidToPersonUidMap
                        )
                    )
                )
            )
        }

        is XapiStatementRef -> {
            //When the object is a statement ref, there are no other entities. Its just a link by uuid
            emptyList()
        }

        is XapiStatement -> {
            this.copy(
                id = Uuid(
                    parentStatementUuid.mostSignificantBits,
                    parentStatementUuid.leastSignificantBits + 1
                ).toString()
            ).toEntities(
                stringHasher = stringHasher,
                primaryKeyManager = primaryKeyManager,
                hasherFactory = hasherFactory,
                json = json,
                xapiSession = xapiSession,
                knownActorUidToPersonUidMap = knownActorUidToPersonUidMap,
                exactJson = null,
                isSubStatement = true,
                learningSpace = learningSpace,
            )
        }

        else -> {
            throw IllegalStateException("This cant really happen. The compiler does not recognize " +
                    "XapiActor as covering XapiGroup and XapiAgent, but it does.")
        }
    }
}

/**
 * https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/json.md#content-based-polymorphic-deserialization
 */
object XapiStatementObjectSerializer: JsonContentPolymorphicSerializer<XapiStatementObject>(XapiStatementObject::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<XapiStatementObject> {

        val objectType = element.jsonObject["objectType"]
            ?.jsonPrimitive?.takeIf { it !is JsonNull }?.content?.let { XapiObjectType.valueOf(it) }
            ?: XapiObjectType.Activity

        return when(objectType) {
            XapiObjectType.Activity -> XapiActivityStatementObject.serializer()
            XapiObjectType.Agent -> XapiAgent.serializer()
            XapiObjectType.Group -> XapiGroup.serializer()
            XapiObjectType.StatementRef -> XapiStatementRef.serializer()
            XapiObjectType.SubStatement -> XapiStatement.serializer()
            else -> throw XapiException(400, "Statement object type invalid")
        }
    }
}
