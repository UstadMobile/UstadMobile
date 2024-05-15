package com.ustadmobile.core.domain.xapi.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class XContext(
    var instructor: Actor? = null,

    var registration: String? = null,

    var language: String? = null,

    var platform: String? = null,

    var revision: String? = null,

    var team: Actor? = null,

    var statement: XObject? = null,

    var contextActivities: ContextActivity? = null,

    var extensions: Map<String, JsonElement>? = null,
)
