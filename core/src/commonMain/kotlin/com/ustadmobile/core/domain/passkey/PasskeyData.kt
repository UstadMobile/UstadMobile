package com.ustadmobile.core.domain.passkey


data class PasskeyData(
    val attestationObj: String,
    val clientDataJson: String,
    val originString: String,
    val rpid: String,
    val challengeString: String,
    val publicKey: String,
    val id: String,
    val personUid :Long?=0
)