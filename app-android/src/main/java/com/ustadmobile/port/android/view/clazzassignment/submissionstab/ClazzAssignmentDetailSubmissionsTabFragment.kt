package com.ustadmobile.port.android.view.clazzassignment.submissionstab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabUiState
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabViewModel
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import androidx.paging.compose.items
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.port.android.util.compose.courseTerminologyEntryResource
import com.ustadmobile.port.android.util.compose.rememberCourseTerminologyEntries
import com.ustadmobile.port.android.util.ext.getContextSupportFragmentManager
import com.ustadmobile.port.android.view.SortBottomSheetFragment
import com.ustadmobile.port.android.view.composable.UstadListSortHeader
import com.ustadmobile.port.android.view.composable.UstadListSpacerItem


class ClazzAssignmentDetailSubmissionsTabFragment: UstadBaseMvvmFragment(){

    private val viewModel by ustadViewModels(::ClazzAssignmentDetailSubmissionsTabViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ClazzAssignmentDetailSubmissionsTabScreen(viewModel)
                }
            }
        }
    }


}

@Composable
fun ClazzAssignmentDetailSubmissionsTabScreen(
    viewModel: ClazzAssignmentDetailSubmissionsTabViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ClazzAssignmentDetailSubmissionsTabUiState())

    val context = LocalContext.current

    ClazzAssignmentDetailSubmissionsTabScreen(
        uiState = uiState,
        onClickPerson = viewModel::onClickSubmitter,
        onClickSort = {
            SortBottomSheetFragment(
                sortOptions = uiState.sortOptions,
                selectedSort = uiState.sortOption,
                onSortOptionSelected = {
                    viewModel.onChangeSortOption(it)
                }
            ).show(context.getContextSupportFragmentManager(), "SortOptions")
        }
    )
}

@Composable
private fun ClazzAssignmentDetailSubmissionsTabScreen(
    uiState: ClazzAssignmentDetailSubmissionsTabUiState,
    onClickPerson: (AssignmentSubmitterSummary) -> Unit = {},
    onClickSort: () -> Unit = {},
) {
    val pager = remember(uiState.assignmentSubmitterList) {
        Pager(
            pagingSourceFactory = uiState.assignmentSubmitterList,
            config = PagingConfig(pageSize = 20, enablePlaceholders = true)
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    val courseTerminologyEntries = rememberCourseTerminologyEntries(
        courseTerminology = uiState.courseTerminology
    )
    val submittersLabel: String = if(uiState.progressSummary?.isGroupAssignment == true) {
        stringResource(R.string.groups)
    }else {
        courseTerminologyEntryResource(
            terminologyEntries = courseTerminologyEntries,
            messageId = MessageID.students
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {

        item(key = "header") {
            Row(
                modifier = Modifier.requiredHeight(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClazzAssignmentSummaryColumn(
                    number = uiState.progressSummary?.totalStudents ?: 0,
                    label = submittersLabel,
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

        item(key = "sort") {
            UstadListSortHeader(
                activeSortOrderOption =uiState.sortOption,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                onClickSort = onClickSort,
            )
        }

        items(
            items = lazyPagingItems,
            key = { person -> person.submitterUid }
        ){ person ->
            SubmitterSummaryListItem(person, onClickPerson)
        }

        UstadListSpacerItem()

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
        assignmentSubmitterList = {
            ListPagingSource(listOf(
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
            ))
        },
    )

    MdcTheme {
        ClazzAssignmentDetailSubmissionsTabScreen(uiStateVal)
    }
}