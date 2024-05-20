package com.ustadmobile.core.domain.xapi.model

import kotlinx.serialization.Serializable

@Serializable
data class XapiContextActivities(
    var parent: List<XapiActivityStatementObject>? = null,

    var grouping: List<XapiActivityStatementObject>? = null,

    var category: List<XapiActivityStatementObject>? = null,

    var other: List<XapiActivityStatementObject>? = null,
)
