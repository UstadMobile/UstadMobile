package com.ustadmobile.core.domain.xapi.model

import kotlinx.serialization.Serializable

@Serializable
data class Verb(
    var id: String? = null,

    var display: Map<String, String>? = null,
)


