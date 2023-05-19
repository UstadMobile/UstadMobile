package com.ustadmobile.core.viewmodel.clazzenrolment.list

import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason



data class ClazzEnrolmentListUiState(
    val enrolmentList: List<ClazzEnrolmentWithLeavingReason> = emptyList(),
    val personName: String? = null,
    var courseName: String? = null,
    val canEditTeacherEnrolments: Boolean = false,
    val canEditStudentEnrolments: Boolean = false,
) {

    fun enrolmentItemUiState(
        enrolment: ClazzEnrolmentWithLeavingReason
    ): ClazzEnrolmentListItemUiState {
        return ClazzEnrolmentListItemUiState(
            enrolment = enrolment,
            canEdit = if(enrolment.clazzEnrolmentRole == ClazzEnrolment.ROLE_TEACHER) {
                canEditTeacherEnrolments
            }else {
                canEditTeacherEnrolments
            }
        )
    }

}

data class ClazzEnrolmentListItemUiState(
    val canEdit: Boolean = false,
    val enrolment: ClazzEnrolmentWithLeavingReason,
)

class ClazzEnrolmentListViewModel {

    companion object {

        const val DEST_NAME = "CourseEnrolments"

    }

}