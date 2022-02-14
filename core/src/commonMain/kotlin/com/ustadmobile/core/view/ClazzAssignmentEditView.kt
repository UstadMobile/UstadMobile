package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer


interface ClazzAssignmentEditView: UstadEditView<ClazzAssignment> {

    var caGracePeriodError: String?
    var caDeadlineError: String?
    var caTitleError: String?
    var caStartDateError: String?
    var caMaxScoreError: String?

    var startDate: Long
    var startTime: Long

    var deadlineDate: Long
    var deadlineTime: Long

    var gracePeriodDate: Long
    var gracePeriodTime: Long

    var timeZone: String?

    var lateSubmissionOptions: List<ClazzAssignmentEditPresenter.LateSubmissionOptionsMessageIdOption>?

    var editAfterSubmissionOptions: List<ClazzAssignmentEditPresenter.EditAfterSubmissionOptionsMessageIdOption>?

    var fileTypeOptions: List<ClazzAssignmentEditPresenter.FileTypeOptionsMessageIdOption>?

    var assignmentTypeOptions: List<ClazzAssignmentEditPresenter.AssignmentTypeOptionsMessageIdOption>?

    var markingTypeOptions: List<ClazzAssignmentEditPresenter.MarkingTypeOptionsMessageIdOption>?

    var clazzAssignmentContent: DoorMutableLiveData<List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>>?

    companion object {

        const val VIEW_NAME = "ClazzAssignmentEditView"

    }

}