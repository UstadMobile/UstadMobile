package com.ustadmobile.core.domain.xapi.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class XapiResult(
    var completion: Boolean? = null,
    var success: Boolean? = null,
    var score: Score? = null,
    var duration: String? = null,
    var response: String? = null,
    var extensions: Map<String, JsonElement>? = null,
) {


    @Serializable
    data class Score(
        var scaled: Float? = null,
        var raw: Float? = null,
        var min: Float? = null,
        var max: Float? = null,
    )

}
