package com.ustadmobile.core.domain.interop.oneroster.model

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.interop.timestamp.format8601Timestamp
import com.ustadmobile.lib.db.composites.StudentResultAndCourseBlockSourcedId

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
)

/**
 * Convert the StudentResult database entitiy to a OneRoster result
 */
fun StudentResultAndCourseBlockSourcedId.toOneRosterResult(
    endpoint: Endpoint,
): Result {
    return Result(
        sourcedId = studentResult.srSourcedId!!,
        status = if(studentResult.srActive) Status.ACTIVE else Status.TOBEDELETED,
        dateLastModified = format8601Timestamp(studentResult.srScoreDate),
        metaData = studentResult.srMetaData,
        lineItem = GUIDRef(
            href = "${endpoint.url}/orhref/lineitem/$cbSourcedId/",
            sourcedId = cbSourcedId ?: studentResult.srCourseBlockUid.toString(),
            type = GuidRefType.lineItem
        ),
        student = GUIDRef(
            href = "${endpoint.url}/orhref/person/${studentResult.srStudentPersonUid}",
            sourcedId = studentResult.srStudentPersonUid.toString(),
            type = GuidRefType.student,
        ),
        score = studentResult.srScore,
        scoreDate = format8601Timestamp(studentResult.srScoreDate),
        comment = studentResult.srComment,
    )
}

