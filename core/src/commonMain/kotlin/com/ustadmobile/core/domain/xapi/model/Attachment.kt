package com.ustadmobile.core.domain.xapi.model

import kotlinx.serialization.Serializable

@Serializable
data class Attachment(
    var usageType: String? = null,

    var display: Map<String, String>? = null,

    var description: Map<String, String>? = null,

    var contentType: String? = null,

    var length: Long = 0,

    var sha2: String? = null,
)
