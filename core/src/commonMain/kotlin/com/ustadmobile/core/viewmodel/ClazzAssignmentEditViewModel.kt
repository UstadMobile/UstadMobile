package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.CourseGroupSet

data class ClazzAssignmentEditUiState(

    val fieldsEnabled: Boolean = true,

    val groupSetEnabled: Boolean = true,

    val caTitleError: String? = null,

    val groupSet: CourseGroupSet? = null,

    val timeZone: String? = null,

    val caStartDateError: String? = null,

    val caMaxPointsError: String? = null,

    val entity: CourseBlockWithEntity? = null,

    val blockWithAssignment: CourseBlock? = null,

    val completionCriteriaOptions: List<ClazzAssignmentEditPresenter.
    CompletionCriteriaOptionsMessageIdOption>? = null,

    val minScoreVisible: Boolean = false,

    val fileSubmissionVisible: Boolean = false,

    val textSubmissionVisible: Boolean = false,

    val markingTypeOptions: List<Int> = listOf(
        ClazzAssignment.MARKED_BY_COURSE_LEADER ,
        ClazzAssignment.MARKED_BY_PEERS
    ),

    val courseBlockEditUiState: CourseBlockEditUiState = CourseBlockEditUiState(),
) {

    val peerMarkingVisible: Boolean
        get() = entity?.assignment?.caMarkingType == ClazzAssignment.MARKED_BY_PEERS

}