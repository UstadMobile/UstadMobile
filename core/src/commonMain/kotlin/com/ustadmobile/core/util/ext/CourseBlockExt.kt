package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation

/**
 * Icon to use in editing screens for this module. If it is a ContentEntry, it will show the
 * content type icon (e.g. video, interactive, etc). Otherwise the CourseBlock icon type should be
 * used (e.g. text, assignment, etc)
 */
val CourseBlockWithEntity.editIconId: Int
    get() = if(cbType == CourseBlock.BLOCK_CONTENT_TYPE) {
        entry?.contentTypeFlag ?: 0
    }else {
        cbType
    }

fun CourseBlock.asCourseBlockWithEntity(
    assignment: ClazzAssignment? = null,
    assignmentPeerReviewAllocations: List<PeerReviewerAllocation>? = null,
    assignmentCourseGroupSetName: String? = null,
): CourseBlockWithEntity {
    return CourseBlockWithEntity().also {
        it.cbUid = cbUid
        it.cbType = cbType
        it.cbIndentLevel = cbIndentLevel
        it.cbModuleParentBlockUid = cbModuleParentBlockUid
        it.cbTitle = cbTitle
        it.cbDescription = cbDescription
        it.cbCompletionCriteria = cbCompletionCriteria
        it.cbHideUntilDate = cbHideUntilDate
        it.cbDeadlineDate = cbDeadlineDate
        it.cbLateSubmissionPenalty = cbLateSubmissionPenalty
        it.cbGracePeriodDate = cbGracePeriodDate
        it.cbMaxPoints = cbMaxPoints
        it.cbMinPoints = cbMinPoints
        it.cbIndex = cbIndex
        it.cbClazzUid = cbClazzUid
        it.cbActive = cbActive
        it.cbHidden = cbHidden
        it.cbEntityUid = cbEntityUid
        it.cbLct = cbLct

        it.assignment = assignment
        it.assignmentPeerAllocations = assignmentPeerReviewAllocations
        it.assignmentCourseGroupSetName = assignmentCourseGroupSetName
    }
}
