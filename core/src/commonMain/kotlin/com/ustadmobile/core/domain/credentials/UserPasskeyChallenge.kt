package com.ustadmobile.core.domain.credentials

import kotlinx.serialization.Serializable

@Serializable
data class UserPasskeyChallenge(
    val username: String,
    val personUid: String,
    val doorNodeId: String,
    val usStartTime: Long,
)