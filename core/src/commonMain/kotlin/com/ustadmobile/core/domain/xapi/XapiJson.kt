package com.ustadmobile.core.domain.xapi

import kotlinx.serialization.json.Json

/**
 * As per the xAPI spec, we should not encode default values (as we do in other areas of the app).
 * This is a simple holder for a Json instance configured accordingly such that it can be easily
 * added to the DI.
 */
data class XapiJson(
    val json: Json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
    }
)
