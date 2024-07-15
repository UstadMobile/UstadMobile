package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.lib.db.entities.xapi.StatementContextActivityJoin
import com.ustadmobile.core.domain.xapi.xapiRequireValidIRI
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.util.ext.toEmptyIfNull
import com.ustadmobile.lib.db.entities.xapi.ActivityEntity
import com.ustadmobile.lib.db.entities.xapi.ActivityExtensionEntity
import com.ustadmobile.lib.db.entities.xapi.ActivityInteractionEntity
import com.ustadmobile.lib.db.entities.xapi.ActivityInteractionEntity.Companion.PROP_CHOICES
import com.ustadmobile.lib.db.entities.xapi.ActivityInteractionEntity.Companion.PROP_SCALE
import com.ustadmobile.lib.db.entities.xapi.ActivityInteractionEntity.Companion.PROP_SOURCE
import com.ustadmobile.lib.db.entities.xapi.ActivityInteractionEntity.Companion.PROP_STEPS
import com.ustadmobile.lib.db.entities.xapi.ActivityInteractionEntity.Companion.PROP_TARGET
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
    val interactionType: XapiInteractionType? = null,
    val correctResponsePattern: List<String>? = null,
    val choices: List<Interaction>? = null,
    val scale: List<Interaction>? = null,
    val source: List<Interaction>? = null,
    val target: List<Interaction>? = null,
    val steps: List<Interaction>? = null,
) {

    @Serializable
    data class Interaction(
        val id: String? = null,
        val description: Map<String, String>? = null
    )
}

/**
 * @param statementContextActivityJoin Join entity used where the activity is part of a Statement's
 * contextActivities
 */
data class ActivityEntities(
    val activityEntity: ActivityEntity,
    val activityLangMapEntries: List<ActivityLangMapEntry> = emptyList(),
    val activityInteractionEntities: List<ActivityInteractionEntity>  = emptyList(),
    val activityExtensionEntities: List<ActivityExtensionEntity> = emptyList(),
    val statementContextActivityJoin: StatementContextActivityJoin? = null,
)

fun XapiActivity?.toEntities(
    activityId: String,
    stringHasher: XXStringHasher,
    json: Json,
): ActivityEntities {
    xapiRequireValidIRI(activityId, "Activity ID is not a valid IRI: $activityId")
    val activityUid = stringHasher.hash(activityId)

    fun Map<String, String>.toLangMapEntries(
        propName: String,
        almeAieHash: Long = 0,
    ) = entries.map { (lang, text) ->
        ActivityLangMapEntry(
            almeActivityUid = activityUid,
            almeHash = stringHasher.hash("$propName-$lang"),
            almeLangCode = lang,
            almeValue = text,
            almeAieHash = almeAieHash,
        )
    }

    fun XapiActivity.Interaction.toEntities(
        propId: Int,
        propName: String,
    ): Pair<ActivityInteractionEntity, List<ActivityLangMapEntry>> {
        val aieHash = stringHasher.hash("$propId$id")

        return ActivityInteractionEntity(
            aieActivityUid = activityUid,
            aieHash = aieHash,
            aieProp = propId,
            aieId = id,
        ) to description?.toLangMapEntries("$propName-$id", almeAieHash = aieHash).toEmptyIfNull()
    }

    val interactionEntitiesAndLangMaps =
        this?.choices?.map { it.toEntities(PROP_CHOICES, "choices") }.toEmptyIfNull() +
        this?.scale?.map { it.toEntities(PROP_SCALE, "scale") }.toEmptyIfNull() +
        this?.source?.map { it.toEntities(PROP_SOURCE, "source") }.toEmptyIfNull() +
        this?.target?.map { it.toEntities(PROP_TARGET, "target") }.toEmptyIfNull() +
        this?.steps?.map { it.toEntities(PROP_STEPS, "steps") }.toEmptyIfNull()

    return ActivityEntities(
        activityEntity = ActivityEntity(
            actUid = activityUid,
            actIdIri = activityId,
            actType = this?.type,
            actMoreInfo = this?.moreInfo,
            actInteractionType = this?.interactionType?.dbFlag ?: ActivityEntity.TYPE_UNSET,
            actCorrectResponsePatterns = this?.correctResponsePattern?.let { json.encodeToString(it) },
        ),
        activityLangMapEntries =
            this?.name?.toLangMapEntries(ActivityLangMapEntry.PROPNAME_NAME).toEmptyIfNull() +
            this?.description?.toLangMapEntries(ActivityLangMapEntry.PROPNAME_DESCRIPTION).toEmptyIfNull() +
            interactionEntitiesAndLangMaps.flatMap { it.second },
        activityInteractionEntities = interactionEntitiesAndLangMaps.map { it.first },
        activityExtensionEntities = this?.extensions?.map { (key, value) ->
            ActivityExtensionEntity(
                aeeActivityUid = activityUid,
                aeeKeyHash = stringHasher.hash(key),
                aeeKey = xapiRequireValidIRI(key, "$activityId extension $key is not a valid IRI"),
                aeeJson = json.encodeToString(JsonElement.serializer(), value)
            )
        } ?: emptyList()
    )
}
