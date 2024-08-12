package com.ustadmobile.core.domain.passkey

data class PasskeyVerifyResult(
    val isVerified:Boolean,
    val personUid:Long
)
