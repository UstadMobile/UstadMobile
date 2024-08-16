package com.ustadmobile.core.domain.xapi.model

import com.benasher44.uuid.Uuid
import com.ustadmobile.lib.db.entities.xapi.StatementContextActivityJoin
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.util.ext.decodeListOrSingleObjectAsList
import com.ustadmobile.core.util.ext.toEmptyIfNull
import com.ustadmobile.lib.db.entities.xapi.ActivityEntity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

/**
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2462-contextactivities-property
 */
@Serializable(with = XapiContextActivitiesSerializer::class)
data class XapiContextActivities(

    val parent: List<XapiActivityStatementObject>? = null,

    val grouping: List<XapiActivityStatementObject>? = null,

    val category: List<XapiActivityStatementObject>? = null,

    val other: List<XapiActivityStatementObject>? = null,
)

@Suppress("unused")
@Serializable
@SerialName("XapiContextActivities")
private class XapiContextActivitiesSurrogate(
    val parent: List<XapiActivityStatementObject>? = null,

    val grouping: List<XapiActivityStatementObject>? = null,

    val category: List<XapiActivityStatementObject>? = null,

    val other: List<XapiActivityStatementObject>? = null,
)

private fun XapiContextActivities.toSurrogate(): XapiContextActivitiesSurrogate {
    return XapiContextActivitiesSurrogate(
        parent = parent, grouping = grouping, category = category, other = other
    )
}

/**
 * Serialization issue: each of the properties can be a single statement or a list of statements as
 * per the xAPI spec.
 *
 * To serialize: this uses the plugin generated serializer of an identical surrogate class (
 * values are always serialized as a list, even if there is only one item in the list)
 *
 * To deserialize: we decode to a JsonElement (using the same logic as used by Kotlinx Serialization's
 * own Polymorphic serializer) so we can determine if any given element is a list or a single object.
 */
object XapiContextActivitiesSerializer : KSerializer<XapiContextActivities> {
    override val descriptor: SerialDescriptor = XapiContextActivitiesSurrogate.serializer().descriptor
    override fun serialize(encoder: Encoder, value: XapiContextActivities) {
        encoder.encodeSerializableValue(XapiContextActivitiesSurrogate.serializer(), value.toSurrogate())
    }

    override fun deserialize(decoder: Decoder): XapiContextActivities {
        //This is the same as what is used in polymorphic serializer
        val jsonDecoder = decoder as? JsonDecoder ?: throw IllegalStateException()
        val jsonObject = jsonDecoder.decodeJsonElement() as JsonObject

        fun propertyToObjectList(propName: String): List<XapiActivityStatementObject>? {
            val jsonElement = jsonObject[propName]
            return if(jsonElement != null) {
                jsonDecoder.json.decodeListOrSingleObjectAsList(
                    XapiActivityStatementObject.serializer(), jsonElement
                )
            }else {
                null
            }
        }

        return XapiContextActivities(
            parent = propertyToObjectList("parent"),
            grouping = propertyToObjectList("grouping"),
            category = propertyToObjectList("category"),
            other = propertyToObjectList("other"),
        )
    }
}

fun List<XapiActivityStatementObject>.toEntities(
    stringHasher: XXStringHasher,
    json: Json,
    statementUuid: Uuid,
    contextType: Int,
) : List<ActivityEntities> {
    return map { contextActivityObj ->
        val activityUid = stringHasher.hash(contextActivityObj.id)
        val scajToHash = stringHasher.hash("$contextType-${contextActivityObj.id}")
        val statementContextActivityJoin = StatementContextActivityJoin(
            scajFromStatementIdHi = statementUuid.mostSignificantBits,
            scajFromStatementIdLo = statementUuid.leastSignificantBits,
            scajToHash = scajToHash,
            scajContextType = contextType,
            scajToActivityUid = activityUid,
            scajToActivityId = contextActivityObj.id,
        )

        contextActivityObj.definition?.toEntities(
            activityId = contextActivityObj.id,
            stringHasher = stringHasher,
            json = json,
        )?.copy(statementContextActivityJoin = statementContextActivityJoin)
            ?: ActivityEntities(
                activityEntity = ActivityEntity(
                    actUid = activityUid,
                    actIdIri = contextActivityObj.id,
                ),
                statementContextActivityJoin = statementContextActivityJoin,
            )
    }
}

fun XapiContextActivities.toEntities(
    stringHasher: XXStringHasher,
    json: Json,
    statementUuid: Uuid,
) : List<ActivityEntities> {
    fun List<XapiActivityStatementObject>?.toEntitiesInternal(type: Int) : List<ActivityEntities> {
        return this?.toEntities(stringHasher, json, statementUuid, type).toEmptyIfNull()
    }

    return parent.toEntitiesInternal(StatementContextActivityJoin.TYPE_PARENT) +
            grouping.toEntitiesInternal(StatementContextActivityJoin.TYPE_GROUPING) +
            category.toEntitiesInternal(StatementContextActivityJoin.TYPE_CATEGORY) +
            other.toEntitiesInternal(StatementContextActivityJoin.TYPE_OTHER)
}
