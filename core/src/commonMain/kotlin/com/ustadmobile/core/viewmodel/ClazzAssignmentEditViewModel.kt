package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity

data class ClazzAssignmentEditUiState(

    val fieldsEnabled: Boolean = true,

    val caTitleError: String? = null,

    val caStartDateError: String? = null,

    val caMaxPointsError: String? = null,

    val entity: CourseBlockWithEntity? = null,

    val blockWithAssignment: CourseBlock? = null,

    val completionCriteriaOptions: List<ClazzAssignmentEditPresenter.
           CompletionCriteriaOptionsMessageIdOption>? = null,

    val minScoreVisible: Boolean = false,
)