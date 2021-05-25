package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer


interface ClazzAssignmentEditView: UstadEditView<ClazzAssignment> {

    var startDate: Long
    var startTime: Long

    var deadlineDate: Long
    var deadlineTime: Long

    var gracePeriodDate: Long
    var gracePeriodTime: Long

    var caGracePeriodError: String?
    var caDeadlineError: String?
    var caTitleError: String?
    var caStartDateError: String?

    var timeZone: String?

    var lateSubmissionOptions: List<ClazzAssignmentEditPresenter.LateSubmissionOptionsMessageIdOption>?

    var clazzAssignmentContent: DoorMutableLiveData<List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>>?

    companion object {

        const val VIEW_NAME = "ClazzAssignmentEditView"

    }

}