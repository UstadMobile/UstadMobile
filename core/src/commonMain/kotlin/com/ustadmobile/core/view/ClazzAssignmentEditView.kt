package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.CourseGroupSet


interface ClazzAssignmentEditView: UstadEditView<CourseBlockWithEntity> {

    var caGracePeriodError: String?
    var caDeadlineError: String?
    var caTitleError: String?
    var caStartDateError: String?
    var caMaxPointsError: String?

    var startDate: Long
    var startTime: Long

    var deadlineDate: Long
    var deadlineTime: Long

    var gracePeriodDate: Long
    var gracePeriodTime: Long

    var timeZone: String?

    var groupSet: CourseGroupSet?

    var submissionPolicyOptions: List<ClazzAssignmentEditPresenter.SubmissionPolicyOptionsMessageIdOption>?

    var fileTypeOptions: List<ClazzAssignmentEditPresenter.FileTypeOptionsMessageIdOption>?

    var textLimitTypeOptions: List<ClazzAssignmentEditPresenter.TextLimitTypeOptionsMessageIdOption>?

    var completionCriteriaOptions: List<ClazzAssignmentEditPresenter.CompletionCriteriaOptionsMessageIdOption>?

    var markingTypeOptions: List<IdOption>?

    var groupSetEnabled: Boolean

    companion object {

        const val VIEW_NAME = "CourseAssignmentEditView"

        const val TERMINOLOGY_ID = "clazzTerminologyId"

    }

}