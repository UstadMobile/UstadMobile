package com.ustadmobile.libuicompose.view.clazzassignment.submissionstab

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabUiState
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabViewModel
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.components.UstadListSpacerItem
import com.ustadmobile.libuicompose.util.compose.courseTerminologyEntryResource
import com.ustadmobile.libuicompose.util.compose.rememberCourseTerminologyEntries
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import dev.icerock.moko.resources.compose.stringResource
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.libuicompose.components.ustadPagedItems
import androidx.compose.runtime.remember
import androidx.paging.compose.collectAsLazyPagingItems
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding


@Composable
fun ClazzAssignmentDetailSubmissionsTabScreen(
    viewModel: ClazzAssignmentDetailSubmissionsTabViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ClazzAssignmentDetailSubmissionsTabUiState())

    ClazzAssignmentDetailSubmissionsTabScreen(
        uiState = uiState,
        onClickPerson = viewModel::onClickSubmitter,
        onChangeSortOption = viewModel::onChangeSortOption
    )
}

@Composable
fun ClazzAssignmentDetailSubmissionsTabScreen(
    uiState: ClazzAssignmentDetailSubmissionsTabUiState,
    onClickPerson: (AssignmentSubmitterSummary) -> Unit = {},
    onChangeSortOption: (SortOrderOption) -> Unit = {}
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
        stringResource(MR.strings.groups)
    }else {
        courseTerminologyEntryResource(
            terminologyEntries = courseTerminologyEntries,
            stringResource = MR.strings.students
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
                    label = stringResource(MR.strings.submitted_key).capitalizeFirstLetter(),
                    addDividerToEnd = true,
                )

                ClazzAssignmentSummaryColumn(
                    number = uiState.progressSummary?.markedStudents ?: 0,
                    label = stringResource(MR.strings.marked_key).capitalizeFirstLetter(),
                )
            }
        }

        item(key = "sort") {
            UstadListSortHeader(
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxWidth(),
                activeSortOrderOption =uiState.sortOption,
                sortOptions = uiState.sortOptions,
                onClickSortOption =  onChangeSortOption
            )
        }

        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { person -> person.submitterUid }
        ){ person ->
            SubmitterSummaryListItem(person, onClickPerson)
        }

        UstadListSpacerItem()

    }
}