package com.ustadmobile.core.domain.credentials.passkey.model

import kotlinx.serialization.Serializable

@Serializable
data class PublicKeyCredentialRpEntity(
    val name: String,
    val id: String,
    val icon: String?,
)
