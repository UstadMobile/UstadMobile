package com.ustadmobile.core.domain.interop.oneroster.model

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.interop.timestamp.format8601Timestamp
import com.ustadmobile.core.domain.interop.timestamp.parse8601Timestamp
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.domain.xxhash.toLongOrHash
import com.ustadmobile.lib.db.entities.CourseBlock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * LineItem as per OneRoster spec section 5.6 ( https://www.imsglobal.org/oneroster-v11-final-specification#_Toc480452039 )
 *
 * As per the spec (section 4) : A class is assessed via a number of line items (columns in a gradebook),
 *
 * This is represented by the CourseBlock in database. Where a OneRoster LineItem is created by an
 * external app, the CourseBlock.cbType is set to CourseBlock.BLOCK_EXTERNAL_APP.
 */
@Serializable
data class LineItem(
    val sourcedId: String,
    val status: Status,
    val dateLastModified: String,
    val title: String,
    val description: String,
    val assignDate: String,
    val dueDate: String,
    @SerialName("class")
    val `class`: GUIDRef,
    val resultValueMin: Float,
    val resultValueMax: Float,
    val metadata: JsonObject? = null,
)

fun CourseBlock.toOneRosterLineItem(
    endpoint: Endpoint,
    json: Json,
): LineItem {
    return LineItem(
        sourcedId = cbSourcedId ?: cbUid.toString(),
        status = if(cbActive) Status.ACTIVE else Status.TOBEDELETED,
        dateLastModified = format8601Timestamp(cbLct),
        title = cbTitle ?: "",
        description = cbDescription ?: "",
        assignDate = format8601Timestamp(cbHideUntilDate),
        dueDate = format8601Timestamp(cbDeadlineDate),
        `class` = GUIDRef(
            href = "${endpoint.url}umapp/#/CourseDetail?entityUid=$cbSourcedId",
            sourcedId = cbClazzSourcedId ?: cbClazzUid.toString(),
            type = GuidRefType.clazz
        ),
        resultValueMin = cbMinPoints ?: 0f,
        resultValueMax = cbMaxPoints ?: 0f,
        metadata = cbMetadata?.let { json.decodeFromString(it) },
    )
}

fun LineItem.toCourseBlock(
    xxHasher: XXStringHasher,
    json: Json,
): CourseBlock {
    return CourseBlock(
        cbSourcedId = sourcedId,
        cbUid = xxHasher.toLongOrHash(sourcedId),
        cbActive = status == Status.ACTIVE,
        cbLct = parse8601Timestamp(dateLastModified),
        cbTitle = title,
        cbDescription = description,
        cbHideUntilDate = parse8601Timestamp(assignDate),
        cbDeadlineDate = parse8601Timestamp(dueDate),
        cbClazzUid =  xxHasher.toLongOrHash(`class`.sourcedId),
        cbClazzSourcedId = `class`.sourcedId,
        cbMinPoints = resultValueMin,
        cbMaxPoints = resultValueMax,
        cbMetadata = metadata?.let { json.encodeToString(JsonObject.serializer(), it) },
    )
}
