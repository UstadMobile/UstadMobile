package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseBlock

data class CourseBlockEditUiState(

    val courseBlock: CourseBlock? = null,

    val fieldsEnabled: Boolean = true,

    val caStartDateError: String? = null,

    val caDeadlineError: String? = null,

    val caMaxPointsError: String? = null,

    val caGracePeriodError: String? = null,

    val minScoreVisible: Boolean = false,

    val gracePeriodVisible: Boolean = false,
)