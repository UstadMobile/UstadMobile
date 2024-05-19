package com.ustadmobile.core.domain.xapi.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Result(
    var completion: Boolean,
    var success: Boolean?,
    var score: Score?,
    var duration: String?,
    var response: String?,
    var extensions: Map<String, JsonElement>? = null
) {


    @Serializable
    data class Score(
        var scaled: Float,
        var raw: Float,
        var min: Float,
        var max: Float,
    )

}
