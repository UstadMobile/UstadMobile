package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.core.domain.xapi.XapiException
import com.ustadmobile.core.domain.xxhash.XXStringHasher
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
 *      objectType: "Activity|Agent|Group|StatementRef
 *      definition: {
 *         ...
 *      }
 *   }
 * So we need to have four different types of XapiStatementObject so the serializer can look at the
 * objectType property to determine the type of the definition object.
 *
 */
@Serializable(with = XapiStatementObjectSerializer::class)
sealed interface XapiStatementObject {
    val id: String
    val objectType: XapiObjectType?
}

@Serializable
data class XapiActivityStatementObject(
    override val objectType: XapiObjectType? = null,
    override val id: String,
    val definition: XapiActivity?,
): XapiStatementObject

@Serializable
data class XapiAgentStatementObject(
    override val objectType: XapiObjectType? = null,
    override val id: String,
    val definition: XapiAgent?,
): XapiStatementObject

@Serializable
data class XapiGroupStatementObject(
    override val objectType: XapiObjectType? = null,
    override val id: String,
    val definition: XapiGroup?,
): XapiStatementObject

@Serializable
data class XapiStatementRefStatementObject(
    override val objectType: XapiObjectType? = null,
    override val id: String,
): XapiStatementObject

@Serializable
data class XapiSubStatementStatementObject(
    override val objectType: XapiObjectType? = null,
    override val id: String,
    val definition: XapiStatement
): XapiStatementObject

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
                contextActivityEntities = listOf(definition.toEntities(id, stringHasher, json))
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
            XapiObjectType.Agent -> XapiAgentStatementObject.serializer()
            XapiObjectType.Group -> XapiGroupStatementObject.serializer()
            XapiObjectType.StatementRef -> XapiStatementRefStatementObject.serializer()
            XapiObjectType.SubStatement -> XapiSubStatementStatementObject.serializer()
            else -> throw XapiException(400, "Statement object type invalid")
        }
    }
}
