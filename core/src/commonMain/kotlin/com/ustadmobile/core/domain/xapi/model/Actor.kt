package com.ustadmobile.core.domain.xapi.model

import kotlinx.serialization.Serializable

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

    @Serializable
    data class Account(
        val name: String? = null,
        val homePage: String? = null
    )
}
