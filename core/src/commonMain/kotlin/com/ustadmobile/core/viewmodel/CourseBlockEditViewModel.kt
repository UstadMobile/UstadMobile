package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseBlock

data class CourseBlockEditUiState(

    val courseBlock: CourseBlock? = null,

    val fieldsEnabled: Boolean = true,

    val caStartDateError: String? = null,

    val caDeadlineError: String? = null,

    val caMaxPointsError: String? = null,

    val caGracePeriodError: String? = null,

    val startTime: Long = 0L,

    val deadlineDate: Long = 0L,

    val deadlineTime: Long = 0L,

    val gracePeriodDate: Long = 0L,

    val gracePeriodTime: Long = 0L,

    val timeZone: String = "US",

    val minScoreVisible: Boolean = false,

    val gracePeriodVisible: Boolean = false,
)