package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.lib.db.entities.XObjectInteractionEntity
import com.ustadmobile.lib.db.entities.XObjectLangMapEntry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Activity definition as per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#activity-definition
 *
 */
@Serializable
@SerialName("Activity")
data class XapiActivity(
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

data class XObjectInteractionEntityAndLangMapEntries(
    val entity: XObjectInteractionEntity,
    val langMapEntries: List<XObjectLangMapEntry>,
)

/**
 * @param property the property for this interaction on the definition e.g.
 * as per XObjectInteractionEntity.PROP_ constants.
 */
fun XapiActivity.Interaction.toEntity(
    xxHasher: XXStringHasher,
    xObjectUid: Long,
    property: Int
) : XObjectInteractionEntityAndLangMapEntries {

    return XObjectInteractionEntityAndLangMapEntries(
        entity = XObjectInteractionEntity(
            xoieObjectUid = xObjectUid,
            xoieIdHash = xxHasher.hash("$property$id"),
            xoieProp = property,
            xoieId = id,
        ),
        langMapEntries = emptyList(),
    )

}