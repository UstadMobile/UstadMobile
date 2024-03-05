package com.ustadmobile.core.domain.assignment.submitmark

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.roundTo
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy

class SubmitMarkUseCase {

    suspend operator fun invoke(
        repo: UmAppDatabase,
        activeUserPersonUid: Long,
        assignmentUid: Long,
        clazzUid: Long,
        submitterUid: Long,
        draftMark: CourseAssignmentMark,
        submissions: List<CourseAssignmentSubmission>,
        courseBlock: CourseBlock,
    ) {
        val applyPenalty = submissions.isNotEmpty() &&
            (submissions.maxOf { it.casTimestamp }) > courseBlock.cbDeadlineDate

        val activeUserSubmitterUid = repo.clazzAssignmentDao.getSubmitterUid(
            assignmentUid = assignmentUid,
            clazzUid = clazzUid,
            accountPersonUid = activeUserPersonUid,
        )

        val markToRecord = draftMark.shallowCopy {
            camAssignmentUid = assignmentUid
            camSubmitterUid = submitterUid
            camMarkerSubmitterUid = activeUserSubmitterUid
            camMarkerPersonUid = activeUserPersonUid
            camMaxMark = courseBlock.cbMaxPoints.toFloat()
            camClazzUid = clazzUid
            if(applyPenalty) {
                camPenalty = (camMark * (courseBlock.cbLateSubmissionPenalty.toFloat()/100f))
                    .roundTo(2)
                camMark -= camPenalty
            }
        }

        repo.courseAssignmentMarkDao.insertAsync(markToRecord)
    }
}