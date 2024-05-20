package com.ustadmobile.core.domain.xapi.model

import kotlinx.serialization.Serializable

@Serializable
data class XapiStatementRef(
    val objectType: XapiObjectType,
    val id: String,
)

