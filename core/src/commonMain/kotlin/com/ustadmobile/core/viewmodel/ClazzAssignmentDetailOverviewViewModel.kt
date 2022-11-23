package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock

data class ClazzAssignmentDetailOverviewUiState(

    val clazzAssignments: List<ClazzAssignmentWithCourseBlock> = emptyList(),

    val caDetailDescriptionVisible: Boolean = false,
    )