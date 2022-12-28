package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.CourseGroupSet

data class CourseGroupSetEditUiState(
    val courseGroupSet: CourseGroupSet? = null,
    val membersList: List<CourseGroupMemberPerson> = emptyList(),
    val courseTitleError: String? = null,
    val numOfGroupsError: String? = null,
    val totalGroupError: String? = null,
    val fieldsEnabled: Boolean = true
)

data class CourseGroupMemberPerson(
    val cgm: CourseGroupMember,
    val name: String
)