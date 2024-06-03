package com.ustadmobile.lib.db.composites

import kotlinx.serialization.Serializable

/**
 * The status of a Block (e.g. CourseBlock, or ContentEntry) for a given student. This is used as a
 * return type (composite) from the DAO.
 * Completion and score information MUST come from a combination of the following:
 *  1) A OneRoster Result entity
 *  2) An xAPI statement
 *  3) A CourseAssignmentMark
 *
 * Maybe convert 1 and 3 into xAPI?
 *
 * @param sCbUid the CourseBlock UID
 * @param sProgress integer between 0 and 100 of progress, if known
 * @param sIsCompleted
 * @param sScoreScaled as per xAPI result (could be the most recent, best, or average).
 *
 */
@Serializable
data class BlockStatus(
    var sPersonUid: Long = 0,
    var sCbUid: Long = 0,
    var sProgress: Int? = null,
    var sIsCompleted: Boolean = false,
    var sIsSuccess: Boolean? = null,
    var sScoreScaled: Float? = null,
) {

    companion object {

        const val STATUS_COMPLETED = 1

        const val STATUS_IN_PROGRESS = 2

    }


}
