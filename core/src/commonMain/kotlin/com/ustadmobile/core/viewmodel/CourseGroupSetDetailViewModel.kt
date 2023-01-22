package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseGroupMemberAndName
import com.ustadmobile.lib.db.entities.CourseGroupSet

data class CourseGroupSetDetailUiState(
    val courseGroupSet: CourseGroupSet? = null,
    val membersList: List<CourseGroupMemberAndName> = emptyList()
)

