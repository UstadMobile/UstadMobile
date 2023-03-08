package com.ustadmobile.core.api.oneroster.model

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.api.oneroster.format8601Timestamp
import com.ustadmobile.core.api.oneroster.parse8601Timestamp
import com.ustadmobile.lib.db.entities.CourseBlock
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
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
) {
}

fun CourseBlock.toOneRosterLineItem(
    endpoint: Endpoint,
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
            sourcedId = cbClazzUid.toString(),
            type = GuidRefType.clazz
        ),
        resultValueMin = cbMinPoints.toFloat(),
        resultValueMax = cbMinPoints.toFloat()
    )
}

fun LineItem.toCourseBlock(): CourseBlock {
    return CourseBlock().apply {
        cbSourcedId = sourcedId
        cbActive = status == Status.ACTIVE
        cbLct = parse8601Timestamp(dateLastModified)
        cbTitle = title
        cbDescription = description
        cbHideUntilDate = parse8601Timestamp(assignDate)
        cbDeadlineDate = parse8601Timestamp(dueDate)
        cbClazzUid = `class`.sourcedId.toLong()
        cbMinPoints = resultValueMin.toInt()
        cbMaxPoints = resultValueMax.toInt()
    }
}
