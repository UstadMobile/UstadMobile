package com.ustadmobile.core.viewmodel.clazzassignment.edit

import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.CourseBlockViewModelConstants
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.DI

data class ClazzAssignmentEditUiState(

    val fieldsEnabled: Boolean = true,

    val markingTypeEnabled: Boolean = true,

    val groupSetEnabled: Boolean = true,

    val caTitleError: String? = null,

    val reviewerCountError: String? = null,

    val groupSet: CourseGroupSet? = null,

    val timeZone: String? = null,

    val caStartDateError: String? = null,

    val caMaxPointsError: String? = null,

    val entity: CourseBlockWithEntity? = null,

    val minScoreVisible: Boolean = false,

    val courseTerminology: CourseTerminology? = null,

    val courseBlockEditUiState: CourseBlockEditUiState = CourseBlockEditUiState(
        completionCriteriaOptions = ASSIGNMENT_COMPLETION_CRITERIAS
    ),
) {

    val peerMarkingVisible: Boolean
        get() = entity?.assignment?.caMarkingType == ClazzAssignment.MARKED_BY_PEERS

    val textSubmissionVisible: Boolean
        get() = entity?.assignment?.caRequireTextSubmission == true

    val fileSubmissionVisible: Boolean
        get()  = entity?.assignment?.caRequireFileSubmission == true


    companion object {

        val ASSIGNMENT_COMPLETION_CRITERIAS = listOf(
            CourseBlockViewModelConstants.CompletionCriteria.ASSIGNMENT_SUBMITTED,
            CourseBlockViewModelConstants.CompletionCriteria.ASSIGNMENT_GRADED
        )

    }
}

class ClazzAssignmentEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadEditViewModel(di, savedStateHandle, ClazzAssignmentEditView.VIEW_NAME) {

    init {

    }
}
