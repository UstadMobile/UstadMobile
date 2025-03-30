package com.ustadmobile.core.domain.credentials

import kotlinx.serialization.Serializable

@Serializable
data class PasskeyVerifyResult(
    val isVerified:Boolean,
    val personUid:Long,
    var firstName: String? = null,
    var lastName: String ? = null,
)
