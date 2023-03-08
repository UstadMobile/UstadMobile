package com.ustadmobile.core.api.oneroster.model

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.api.oneroster.format8601Timestamp
import com.ustadmobile.core.api.oneroster.parse8601Timestamp
import com.ustadmobile.core.util.parse8601Duration
import com.ustadmobile.lib.db.entities.StudentResult
import com.ustadmobile.lib.db.entities.StudentResultAndSourcedIds

/**
 * Result as per section 4.11 of oneroster spec
 */
@kotlinx.serialization.Serializable
data class Result(
    val sourcedId: String,
    val status: Status,
    val dateLastModified: String,
    val metaData: String?,
    val lineItem: GUIDRef,
    val student: GUIDRef,
    val score: Float,
    val scoreDate: String,
    val comment: String?,
) {

}

fun StudentResultAndSourcedIds.toOneRosterResult(
    endpoint: Endpoint,
): Result {
    val courseBlockSourcedId = cbSourcedId ?: studentResult.srCourseBlockUid.toString()
    return Result(
        sourcedId = studentResult.srSourcedId,
        status = if(studentResult.srActive) Status.ACTIVE else Status.TOBEDELETED,
        dateLastModified = format8601Timestamp(studentResult.srScoreDate),
        metaData = studentResult.srMetaData,
        lineItem = GUIDRef(
            href = "${endpoint.url}/orhref/lineitem/$courseBlockSourcedId/",
            sourcedId = courseBlockSourcedId,
            type = GuidRefType.lineItem
        ),
        student = GUIDRef(
            href = "${endpoint.url}/orhref/person/${studentResult.srStudentPersonUid}",
            sourcedId = studentResult.srStudentPersonUid.toString(),
            type = GuidRefType.student,
        ),
        score = studentResult.srScore,
        scoreDate = format8601Timestamp(studentResult.srScoreDate),
        comment = studentResult.srComment
    )
}

fun Result.toStudentResult() : StudentResult{
    return StudentResult(
        srSourcedId = sourcedId,
        srActive = status == Status.ACTIVE,
        srLastModified = parse8601Timestamp(dateLastModified),
        srMetaData = metaData,
        srLineItemSourcedId = lineItem.sourcedId,
        srStudentPersonUid = student.sourcedId.toLong(),
        srScore = score,
        srScoreDate = parse8601Timestamp(scoreDate),
        srComment = comment
    )

}

