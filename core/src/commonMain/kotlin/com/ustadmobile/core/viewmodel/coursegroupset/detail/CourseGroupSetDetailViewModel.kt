package com.ustadmobile.core.viewmodel.coursegroupset.detail

import com.ustadmobile.lib.db.entities.CourseGroupMemberAndName
import com.ustadmobile.lib.db.entities.CourseGroupSet

data class CourseGroupSetDetailUiState(
    val courseGroupSet: CourseGroupSet? = null,
    val membersList: List<CourseGroupMemberAndName> = emptyList()
)

class CourseGroupSetDetailViewModel {

    companion object {

        const val DEST_NAME = "CourseGroups"

    }
}