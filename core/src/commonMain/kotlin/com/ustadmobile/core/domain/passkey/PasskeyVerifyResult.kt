package com.ustadmobile.core.domain.passkey

import kotlinx.serialization.Serializable

@Serializable
data class PasskeyVerifyResult(
    val isVerified:Boolean,
    val personUid:Long
)
