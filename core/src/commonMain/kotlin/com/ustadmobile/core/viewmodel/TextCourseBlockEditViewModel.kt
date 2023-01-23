package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseBlock

data class TextCourseBlockEditUiState(

    val block: CourseBlock? = null,

    val blockTitleError: String? = null,

    val startDate: Long = 0,

    val startTime: Int = 0,

    val fieldsEnabled: Boolean = true,

)