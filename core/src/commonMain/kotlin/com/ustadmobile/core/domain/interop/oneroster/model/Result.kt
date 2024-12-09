package com.ustadmobile.core.domain.interop.oneroster.model

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.interop.timestamp.format8601Timestamp
import com.ustadmobile.core.domain.interop.timestamp.parse8601Timestamp
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.domain.xxhash.toLongOrHash
import com.ustadmobile.lib.db.composites.StudentResultAndCourseBlockSourcedId
import com.ustadmobile.lib.db.entities.StudentResult

/**
 * Result as per section 4.11 of the OneRoster spec. As per the spec (section 4):
 *
 * "Students taking a class are assessed by grading; a lineItem will have zero or more results,
 * but usually only one result per student"
 *
 * Result is represented in the database by StudentResult.
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
    learningSpace: LearningSpace,
): Result {
    return Result(
        sourcedId = studentResult.srSourcedId!!,
        status = Status.fromIsDeletedBool(studentResult.srDeleted),
        dateLastModified = format8601Timestamp(studentResult.srScoreDate),
        metaData = studentResult.srMetaData,
        lineItem = GUIDRef(
            href = "${learningSpace.url}/orhref/lineitem/$cbSourcedId/",
            sourcedId = cbSourcedId ?: studentResult.srCourseBlockUid.toString(),
            type = GuidRefType.lineItem
        ),
        student = GUIDRef(
            href = "${learningSpace.url}/orhref/person/${studentResult.srStudentPersonUid}",
            sourcedId = studentResult.srStudentPersonUid.toString(),
            type = GuidRefType.student,
        ),
        score = studentResult.srScore,
        scoreDate = format8601Timestamp(studentResult.srScoreDate),
        comment = studentResult.srComment,
    )
}


fun Result.toStudentResult(
    xxHasher: XXStringHasher,
    clazzUid: Long,
) : StudentResult {
    return StudentResult(
        srUid = xxHasher.hash(sourcedId),
        srSourcedId = sourcedId,
        srLineItemSourcedId = lineItem.sourcedId,
        srLineItemHref = lineItem.href,
        srCourseBlockUid = xxHasher.toLongOrHash(lineItem.sourcedId),
        srDeleted = status == Status.TOBEDELETED,
        srLastModified = parse8601Timestamp(dateLastModified),
        srMetaData = metaData,
        srStudentPersonUid = xxHasher.toLongOrHash(student.sourcedId),
        srScore = score,
        srScoreDate = parse8601Timestamp(scoreDate),
        srComment = comment,
        srClazzUid = clazzUid,
    )

}

