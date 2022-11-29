package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseBlock

data class CourseBlockEditUiState(

    val courseBlock: CourseBlock? = null,

    val fieldsEnabled: Boolean = true
)