package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.core.domain.xapi.XapiException
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.door.DoorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.xapi.ActorEntity
import com.ustadmobile.lib.db.entities.xapi.GroupMemberActorJoin
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * An XapiActor can be an Agent or Group as per the spec
 */
@Serializable(with = XapiActorSerializer::class)
sealed interface XapiActor {
    val name: String?
    val mbox: String?
    val mbox_sha1sum: String?
    val openid: String?
    val objectType: XapiObjectType?
    val account: XapiAccount?
}

fun XapiActor.identifierHash(xxHasher: XXStringHasher) :Long {
    val idStr = when {
        account != null -> "${account?.name}@${account?.homePage}"
        mbox != null -> mbox
        mbox_sha1sum != null -> mbox_sha1sum
        openid != null -> openid
        else -> null
    }

    return idStr?.let { xxHasher.hash(it) } ?: 0
}

object XapiActorSerializer: JsonContentPolymorphicSerializer<XapiActor>(XapiActor::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<XapiActor> {
        val objectType = element.jsonObject["type"]
            ?.jsonPrimitive?.content?.let { XapiObjectType.valueOf(it) }
            ?: XapiObjectType.Agent

        return when (objectType) {
            XapiObjectType.Agent -> XapiAgent.serializer()
            XapiObjectType.Group -> XapiGroup.serializer()
            else -> throw XapiException(400, "Invalid object type for actor: must be Agent or Group")
        }
    }
}

data class ActorEntities(
    val actor: ActorEntity,
    val groupMemberAgents: List<ActorEntity> = emptyList(),
    val groupMemberJoins: List<GroupMemberActorJoin> = emptyList(),
)

fun XapiActor.toEntities(
    stringHasher: XXStringHasher,
    primaryKeyManager: DoorPrimaryKeyManager,
    hasherFactory: XXHasher64Factory,
    knownActorUidToPersonUidMap: Map<Long, Long> = emptyMap(),
): ActorEntities {
    return when(this) {
        is XapiAgent -> ActorEntities(toActorEntity(stringHasher, knownActorUidToPersonUidMap))
        is XapiGroup -> toGroupEntities(
            stringHasher, primaryKeyManager, hasherFactory, knownActorUidToPersonUidMap
        )
    }
}
