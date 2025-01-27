package com.ustadmobile.core.domain.invite

import kotlinx.serialization.Serializable


@Serializable
data class ResendInviteRequest(
    val contacts: String,
    val personUid: Long
)