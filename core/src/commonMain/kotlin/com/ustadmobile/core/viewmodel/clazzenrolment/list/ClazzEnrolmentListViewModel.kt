package com.ustadmobile.core.viewmodel.clazzenrolment.list

import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.CourseTerminology


data class ClazzEnrolmentListUiState(
    val enrolmentList: List<ClazzEnrolmentWithLeavingReason> = emptyList(),
    val personName: String? = null,
    var courseName: String? = null,
    val canEditTeacherEnrolments: Boolean = false,
    val canEditStudentEnrolments: Boolean = false,
    val courseTerminology: CourseTerminology? = null,
    val timeZone: String = "UTC",
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
            },
            timeZone = timeZone,
        )
    }

}

data class ClazzEnrolmentListItemUiState(
    val canEdit: Boolean = false,
    val enrolment: ClazzEnrolmentWithLeavingReason,
    val timeZone: String,
)

class ClazzEnrolmentListViewModel {

    companion object {

        const val DEST_NAME = "CourseEnrolments"

    }

}