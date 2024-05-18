package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.core.domain.xxhash.XXHasher
import com.ustadmobile.lib.db.entities.AgentEntity
import kotlinx.serialization.Serializable

/**
 * For serializing/deserializing purposes, this class contains properties
 * for all possible actor types (agent, group).
 */
@Serializable
data class Actor(
    val name: String? = null,
    val mbox: String? = null,
    val mbox_sha1sum: String? = null,
    val openid: String? = null,
    val objectType: String? = null,
    val members: List<Actor>? = null,
    val account: Account? = null
) {

    fun isAgent() = mbox != null || openid != null || account != null

    @Serializable
    data class Account(
        val name: String? = null,
        val homePage: String? = null
    )
}

fun Actor.toAgentEntity(
    xxHasher: XXHasher
) : AgentEntity {
    val idStr = when {
        account != null -> "${account.name}@${account.homePage}"
        mbox != null -> mbox
        openid != null -> openid
        else -> throw IllegalArgumentException("Actor is not an agent: no identifier property")
    }

    return AgentEntity(
        agentUid = xxHasher.hash(idStr),
        agentMbox = mbox,
        agentMbox_sha1sum = mbox_sha1sum,
        agentOpenid = openid,
        agentAccountName = account?.name,
        agentHomePage = account?.homePage,
    )
}

