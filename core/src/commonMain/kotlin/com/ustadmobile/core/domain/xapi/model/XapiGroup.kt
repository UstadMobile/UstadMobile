package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.util.ext.toByteArray
import com.ustadmobile.door.DoorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.xapi.ActorEntity
import com.ustadmobile.lib.db.entities.xapi.GroupMemberActorJoin
import com.ustadmobile.lib.db.entities.xapi.XapiEntityObjectTypeFlags
import kotlinx.serialization.Serializable

/**
 * As per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2422-when-the-actor-objecttype-is-group
 *
 * Note: the member list may be returned in any order as per the xAPI spec, so we can reorder it to
 * produce a consistent hash
 */
@Serializable
data class XapiGroup(
    override val name: String? = null,
    override val mbox: String? = null,
    override val mbox_sha1sum: String? = null,
    override val openid: String? = null,
    override val objectType: XapiObjectType? = null,
    override val account: XapiAccount? = null,
    val member: List<XapiAgent> = emptyList(),
): XapiActor, XapiStatementObject

val XapiGroup.isAnonymous: Boolean
    get() = mbox == null && openid == null && account == null

/**
 * If Group is anonymous:
 *   The group will never be modified. The Actor.actorUid for the ActorEntity representing the group
 *   itself will be created by door primary key manager. All GroupMemberActorJoins will be new.
 *   As per the Xapi Spec :
 *   https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2422-when-the-actor-objecttype-is-group
 *   "A Learning Record Consumer MUST consider each Anonymous Group distinct even if it has an
 *   identical set of members."
 *
 * If the group is identified:
 *   The group members might be updated. The Actor.actorUid uses the identifierHash (e.g. the same
 *   as an Agent). AccountEtag will be a hash of hashes of the member agents.
 *
 *   The GroupMemberActorJoins will only change IF the AccountEtag was changed (e.g. the an
 *   ActorEntity representing an identified group has a AccountEtag to the already existing one,
 *   then GroupMemberActorJoins need to be inserted if not already existing, or the last modified
 *   time needs to be updated and set to be the same as Actor.actorLct for the entity representing
 *   the group).
 *
 *   Matching the last modified times makes it possible to query the current members of the group
 *   by matching the ActorEntity.actorLct and GroupMemberActorJoin.gmajLastMod.
 */
fun XapiGroup.toGroupEntities(
    stringHasher: XXStringHasher,
    primaryKeyManager: DoorPrimaryKeyManager,
    hasherFactory: XXHasher64Factory,
    knownActorUidToPersonUidMap: Map<Long, Long> = emptyMap(),
) : ActorEntities {
    val modTime = systemTimeInMillis()

    val memberActors = member.map {
        it.identifierHash(stringHasher) to it.toActorEntity(stringHasher, knownActorUidToPersonUidMap)
    }.sortedBy { it.first }

    val hasher = hasherFactory.newHasher(0)
    memberActors.forEach {
        hasher.update(it.first.toByteArray())
    }
    val memberHash = hasher.digest()

    val groupActor = ActorEntity(
        actorUid = if(isAnonymous) {
            primaryKeyManager.nextId(ActorEntity.TABLE_ID)
        }else {
            identifierHash(stringHasher)
        },
        actorObjectType = XapiEntityObjectTypeFlags.GROUP,
        actorName = name,
        actorMbox = mbox,
        actorMbox_sha1sum = mbox_sha1sum,
        actorOpenid = openid,
        actorAccountName = account?.name,
        actorAccountHomePage = account?.homePage,
        actorLct = modTime,
        actorEtag = memberHash,
    )


    return ActorEntities(
        actor = groupActor,
        groupMemberAgents = memberActors.map { it.second },
        groupMemberJoins = memberActors.map {
            GroupMemberActorJoin(
                gmajGroupActorUid = groupActor.actorUid,
                gmajMemberActorUid = it.second.actorUid,
                gmajLastMod = modTime,
            )
        }
    )
}
