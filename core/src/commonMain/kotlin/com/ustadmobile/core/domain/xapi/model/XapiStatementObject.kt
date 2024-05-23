package com.ustadmobile.core.domain.xapi.model

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.domain.xapi.XapiException
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.lib.db.entities.xapi.XapiEntityObjectTypeFlags
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
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
    val definition: XapiActivity?,
): XapiStatementObject

/**
 * As per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#object-is-statement
 */
@Serializable
data class XapiStatementRefStatementObject(
    override val objectType: XapiObjectType? = null,
    val id: String,
): XapiStatementObject

@Serializable
data class XapiSubStatementStatementObject(
    override val objectType: XapiObjectType? = null,
    val id: String,
    val definition: XapiStatement
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
        is XapiStatementRefStatementObject -> {
            val uuid = uuidFrom(id)
            Pair(uuid.mostSignificantBits, uuid.leastSignificantBits)
        }
        is XapiSubStatementStatementObject -> {
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
fun XapiStatementObject.toEntities(
    stringHasher: XXStringHasher,
    json: Json
) : StatementEntities {
    return when(this) {
        is XapiActivityStatementObject -> {
            StatementEntities(
                activityEntities = listOf(definition.toEntities(id, stringHasher, json))
            )
        }
        else -> { TODO() }
    }
}

/**
 * https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/json.md#content-based-polymorphic-deserialization
 */
object XapiStatementObjectSerializer: JsonContentPolymorphicSerializer<XapiStatementObject>(XapiStatementObject::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<XapiStatementObject> {

        val objectType = element.jsonObject["type"]
            ?.jsonPrimitive?.content?.let { XapiObjectType.valueOf(it) }
            ?: XapiObjectType.Activity

        return when(objectType) {
            XapiObjectType.Activity -> XapiActivityStatementObject.serializer()
            XapiObjectType.Agent -> XapiAgent.serializer()
            XapiObjectType.Group -> XapiGroup.serializer()
            XapiObjectType.StatementRef -> XapiStatementRefStatementObject.serializer()
            XapiObjectType.SubStatement -> XapiSubStatementStatementObject.serializer()
            else -> throw XapiException(400, "Statement object type invalid")
        }
    }
}
