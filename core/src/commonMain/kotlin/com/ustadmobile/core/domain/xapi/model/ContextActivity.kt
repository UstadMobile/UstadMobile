package com.ustadmobile.core.domain.xapi.model

import kotlinx.serialization.Serializable

@Serializable
data class ContextActivity(
    var parent: List<XObject>? = null,

    var grouping: List<XObject>? = null,

    var category: List<XObject>? = null,

    var other: List<XObject>? = null,
)
