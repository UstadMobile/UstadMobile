package com.ustadmobile.core.domain.xapi.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Activity definitino as per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#activity-definition
 *
 */
@Serializable
data class Definition(
    val name: Map<String, String>? = null,
    val description: Map<String, String>? = null,
    val type: String? = null,
    val extensions: Map<String, JsonElement>? = null,
    val moreInfo: String? = null,
    val interactionType: String? = null,
    val correctResponsePattern: List<String>? = null,
    val choices: List<Interaction>? = null,
    val scale: List<Interaction>? = null,
    val source: List<Interaction>? = null,
    val target: List<Interaction>? = null,
    val steps: List<Interaction>? = null
) {

    @Serializable
    data class Interaction(
        val id: String? = null,
        val description: Map<String, String>? = null
    )
}

