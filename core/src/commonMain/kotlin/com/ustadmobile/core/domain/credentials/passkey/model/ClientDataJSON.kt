package com.ustadmobile.core.domain.credentials.passkey.model

import kotlinx.serialization.Serializable

@Serializable
data class ClientDataJSON(
    val type: String,
    val challenge: String,
    val origin: String,
    val crossOrigin: Boolean?=null
)