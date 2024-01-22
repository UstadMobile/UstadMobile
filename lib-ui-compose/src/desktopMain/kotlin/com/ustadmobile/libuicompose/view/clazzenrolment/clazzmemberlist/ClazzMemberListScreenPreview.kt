package com.ustadmobile.libuicompose.view.clazzenrolment.clazzmemberlist

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListUiState
import com.ustadmobile.lib.db.composites.PersonAndClazzMemberListDetails

@Composable
@Preview
fun ClazzMemberListScreenPreview() {
    val uiStateVal = ClazzMemberListUiState(
//        studentList = {
//            ListPagingSource(listOf(
//                PersonWithClazzEnrolmentDetails().apply {
//                    personUid = 1
//                    firstNames = "Student 1"
//                    lastName = "Name"
//                    attendance = 20F
//                },
//                PersonWithClazzEnrolmentDetails().apply {
//                    personUid = 3
//                    firstNames = "Student 3"
//                    lastName = "Name"
//                    attendance = 65F
//                }
//            ))
//        },
//        pendingStudentList = {
//            ListPagingSource(listOf(
//                PersonWithClazzEnrolmentDetails().apply {
//                    personUid = 1
//                    firstNames = "Student 1"
//                    lastName = "Name"
//                    attendance = 20F
//                }
//            ))
//        },
//        teacherList = {
//            ListPagingSource(listOf(
//                PersonWithClazzEnrolmentDetails().apply {
//                    personUid = 1
//                    firstNames = "Teacher 1"
//                    lastName = "Name"
//                }
//            ))
//        },
        addStudentVisible = true,
        addTeacherVisible = true
    )

    ClazzMemberListScreen(
        uiState = uiStateVal,
        onClickPendingRequest = {
                enrolment: PersonAndClazzMemberListDetails,
                approved: Boolean ->  {}
        }
    )
}