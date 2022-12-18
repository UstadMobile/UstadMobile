package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock

data class ClazzAssignmentDetailOverviewUiState(

    val entity: ClazzAssignmentWithCourseBlock? = null,

    val caDetailDescriptionVisible: Boolean = false,
) {

    val caDescriptionVisible: Boolean
        get() = !entity?.caDescription.isNullOrBlank()

    val cbDeadlineDateVisible: Boolean
        get() = entity?.block?.cbDeadlineDate.isDateSet()
}