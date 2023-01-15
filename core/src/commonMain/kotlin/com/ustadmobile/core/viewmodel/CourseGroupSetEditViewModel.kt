package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseGroupMemberAndName
import com.ustadmobile.lib.db.entities.CourseGroupSet

data class CourseGroupSetEditUiState(
    val courseGroupSet: CourseGroupSet? = null,
    val membersList: List<CourseGroupMemberAndName> = emptyList(),
    val courseTitleError: String? = null,
    val numOfGroupsError: String? = null,
    val totalGroupError: String? = null,
    val fieldsEnabled: Boolean = true
)