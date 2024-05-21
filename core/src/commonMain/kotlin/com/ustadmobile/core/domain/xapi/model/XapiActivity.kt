package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.core.domain.xapi.model.XapiActivity.Companion.PROPNAME_NAME
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.util.ext.toEmptyIfNull
import com.ustadmobile.lib.db.entities.xapi.ActivityEntity
import com.ustadmobile.lib.db.entities.xapi.ActivityInteractionEntity
import com.ustadmobile.lib.db.entities.xapi.ActivityLangMapEntry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    companion object {

        const val PROPNAME_NAME = "name"

        const val PROPNAME_DESCRIPTION = "description"

    }
}

data class ActivityEntities(
    val activityEntity: ActivityEntity,
    val activityLangMapEntries: List<ActivityLangMapEntry>,
    val activityInteractionEntities: List<ActivityInteractionEntity>,
)

fun XapiActivity?.toEntities(
    id: String,
    stringHasher: XXStringHasher,
    json: Json,
): ActivityEntities {
    val activityUid = stringHasher.hash(id)

    fun Map<String, String>.toLangMapEntries(propName: String) = entries.map { (lang, text) ->
        ActivityLangMapEntry(
            almeActivityUid = activityUid,
            almeHash = stringHasher.hash("$propName-$lang"),
            almeLangCode = lang,
            almeEntry = text,
        )
    }

    return ActivityEntities(
        activityEntity = ActivityEntity(
            actUid = activityUid,
            actIdIri = id,
            actType = this?.type,
            actMoreInfo = this?.moreInfo,
            actInteractionType = this?.interactionType,
            actCorrectResponsePatterns = this?.correctResponsePattern?.let { json.encodeToString(it) },
        ),
        activityLangMapEntries =
            this?.name?.toLangMapEntries(PROPNAME_NAME).toEmptyIfNull() +
            this?.description?.toLangMapEntries(XapiActivity.PROPNAME_DESCRIPTION).toEmptyIfNull(),
        activityInteractionEntities = emptyList(),
    )
}
