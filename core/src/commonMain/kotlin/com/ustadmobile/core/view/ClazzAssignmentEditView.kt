package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.lib.db.entities.ClazzAssignment


interface ClazzAssignmentEditView: UstadEditView<ClazzAssignment> {

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

    var editAfterSubmissionOptions: List<ClazzAssignmentEditPresenter.EditAfterSubmissionOptionsMessageIdOption>?

    var fileTypeOptions: List<ClazzAssignmentEditPresenter.FileTypeOptionsMessageIdOption>?

    var textLimitTypeOptions: List<ClazzAssignmentEditPresenter.TextLimitTypeOptionsMessageIdOption>?

    var submissionTypeOptions: List<ClazzAssignmentEditPresenter.SubmissionTypeOptionsMessageIdOption>?

    var completionCriteriaOptions: List<ClazzAssignmentEditPresenter.CompletionCriteriaOptionsMessageIdOption>?

    var markingTypeOptions: List<ClazzAssignmentEditPresenter.MarkingTypeOptionsMessageIdOption>?

    companion object {

        const val VIEW_NAME = "ClassAssignmentEditView"

    }

}