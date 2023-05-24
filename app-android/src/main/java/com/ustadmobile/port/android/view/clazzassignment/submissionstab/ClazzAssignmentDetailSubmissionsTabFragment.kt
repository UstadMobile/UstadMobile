package com.ustadmobile.port.android.view.clazzassignment.submissionstab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabUiState
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment


class ClazzAssignmentDetailSubmissionsTabFragment: UstadBaseMvvmFragment(){


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {

        }
    }


}

@Composable
private fun ClazzAssignmentDetailSubmissionsTabScreen(
    uiState: ClazzAssignmentDetailSubmissionsTabUiState,
    onClickPerson: (AssignmentSubmitterSummary) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {

        item {
            Row(
                modifier = Modifier.requiredHeight(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClazzAssignmentSummaryColumn(
                    number = uiState.progressSummary?.calculateNotSubmittedStudents() ?: 0,
                    label = stringResource(R.string.not_started),
                    addDividerToEnd = true,
                )

                ClazzAssignmentSummaryColumn(
                    number = uiState.progressSummary?.submittedStudents ?: 0,
                    label = stringResource(R.string.submitted).capitalizeFirstLetter(),
                    addDividerToEnd = true,
                )

                ClazzAssignmentSummaryColumn(
                    number = uiState.progressSummary?.markedStudents ?: 0,
                    label = stringResource(R.string.marked).capitalizeFirstLetter(),
                )
            }
        }

        items(
            items = uiState.assignmentSubmitterList,
            key = { person -> person.submitterUid }
        ){ person ->
            SubmitterSummaryListItem(person, onClickPerson)
        }

    }
}

@Composable
@Preview
fun ClazzAssignmentDetailStudentProgressListOverviewScreenPreview() {
    val uiStateVal = ClazzAssignmentDetailSubmissionsTabUiState(
        progressSummary = AssignmentProgressSummary().apply {
            totalStudents = 10
            submittedStudents = 2
            markedStudents = 3
        },
        assignmentSubmitterList = listOf(
            AssignmentSubmitterSummary().apply {
                submitterUid = 1
                name = "Bob Dylan"
                latestPrivateComment = "Here is private comment"
                fileSubmissionStatus = CourseAssignmentSubmission.MARKED
            },
            AssignmentSubmitterSummary().apply {
                submitterUid = 2
                name = "Morris Rogers"
                latestPrivateComment = "Here is private comment"
                fileSubmissionStatus = CourseAssignmentSubmission.SUBMITTED
            }
        ),
    )

    MdcTheme {
        ClazzAssignmentDetailSubmissionsTabScreen(uiStateVal)
    }
}