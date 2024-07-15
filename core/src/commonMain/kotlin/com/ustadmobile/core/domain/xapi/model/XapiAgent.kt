package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.xapi.ActorEntity
import com.ustadmobile.lib.db.entities.xapi.XapiEntityObjectTypeFlags
import kotlinx.serialization.Serializable

/**
 *
 */
@Serializable
data class XapiAgent(
    override val name: String? = null,
    override val mbox: String? = null,
    override val mbox_sha1sum: String? = null,
    override val openid: String? = null,
    override val objectType: XapiObjectType? = null,
    override val account: XapiAccount? = null,
): XapiActor, XapiStatementObject

fun XapiAgent.toActorEntity(
    xxHasher: XXStringHasher,
    knownActorUidToPersonUidMap: Map<Long, Long>,
    lastModifiedTime: Long = systemTimeInMillis(),
) : ActorEntity {
    val uid = identifierHash(xxHasher)
    return ActorEntity(
        actorUid = uid,
        actorPersonUid = knownActorUidToPersonUidMap[uid] ?: 0L,
        actorMbox = mbox,
        actorMbox_sha1sum = mbox_sha1sum,
        actorOpenid = openid,
        actorAccountName = account?.name,
        actorAccountHomePage = account?.homePage,
        actorLct = lastModifiedTime,
        actorEtag = name?.let { xxHasher.hash(it) } ?: 0,
        actorObjectType = XapiEntityObjectTypeFlags.AGENT,
    )
}
