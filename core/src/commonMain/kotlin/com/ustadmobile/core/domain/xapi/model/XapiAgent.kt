package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.xapi.ActorEntity
import com.ustadmobile.lib.db.entities.xapi.XapiEntityObjectTypeFlags
import kotlinx.serialization.Serializable

/**
 * Needs to be joinable to person -
 *  Ideally: xapiIdentifierHash should be added to the person entity and set when there is a userame
 *
 *  a) could link by username
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
    lastModifiedTime: Long = systemTimeInMillis(),
) : ActorEntity {
    return ActorEntity(
        actorUid = identifierHash(xxHasher),
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