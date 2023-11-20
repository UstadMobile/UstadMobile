package com.ustadmobile.libuicompose.view.clazzenrolment.list

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListUiState
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.LeavingReason

@Composable
@Preview
fun ClazzEnrolmentListPreview(){
    ClazzEnrolmentListScreen(
        uiState = ClazzEnrolmentListUiState(
            personName = "Ahmad",
            courseName = "Mathematics",
            enrolmentList = listOf(
                ClazzEnrolmentWithLeavingReason().apply {
                    clazzEnrolmentDateJoined = 349880298
                    clazzEnrolmentDateLeft = 509823093
                    clazzEnrolmentUid = 7
                    clazzEnrolmentRole = 1000
                    clazzEnrolmentOutcome = 201
                },
                ClazzEnrolmentWithLeavingReason().apply {
                    clazzEnrolmentDateJoined = 349887338
                    clazzEnrolmentDateLeft = 409937093
                    clazzEnrolmentUid = 8
                    clazzEnrolmentRole = 1000
                    clazzEnrolmentOutcome = 203
                    leavingReason = LeavingReason().apply {
                        leavingReasonTitle = "transportation problem"
                    }
                }
            )
        )
    )
}