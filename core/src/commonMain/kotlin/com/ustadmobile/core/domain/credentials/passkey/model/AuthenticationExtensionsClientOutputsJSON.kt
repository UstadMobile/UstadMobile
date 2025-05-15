package com.ustadmobile.core.domain.credentials.passkey.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationExtensionsClientOutputsJSON(
    val prf: AuthenticationExtensionsPRFOutputsJSON? = null
)