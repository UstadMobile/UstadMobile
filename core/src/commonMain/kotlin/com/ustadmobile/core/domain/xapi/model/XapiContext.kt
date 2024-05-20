package com.ustadmobile.core.domain.xapi.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class XapiContext(
    var instructor: XapiActor? = null,

    var registration: String? = null,

    var language: String? = null,

    var platform: String? = null,

    var revision: String? = null,

    var team: XapiActor? = null,

    var statement: XapiActivityStatementObject? = null,

    var contextActivities: XapiContextActivities? = null,

    var extensions: Map<String, JsonElement>? = null,
)
