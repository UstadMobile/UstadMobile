package com.ustadmobile.libuicompose.view.report.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.report.list.ReportListUiState
import com.ustadmobile.core.viewmodel.report.list.ReportListViewModel
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.libuicompose.components.UstadAddListItem
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun ReportListScreen(
    viewModel: ReportListViewModel
) {
    val uiState: ReportListUiState by viewModel.uiState.collectAsState(ReportListUiState())

    ReportListScreen(
        uiState = uiState,
        onListItemClick = viewModel::onClickEntry,
        onClickAddNew = viewModel::onClickAdd,
        listRefreshCommand = viewModel.refreshCommandFlow,
        onRemoveReport = viewModel::onRemoveReport
    )
}

@Composable
fun ReportListScreen(
    uiState: ReportListUiState,
    onListItemClick: (Report) -> Unit,
    onClickAddNew: () -> Unit,
    listRefreshCommand: Flow<RefreshCommand> = emptyFlow(),
    onRemoveReport: (Long) -> Unit

) {
    val doorRepoPager = rememberDoorRepositoryPager(
        pagingSourceFactory = uiState.reportList,
        refreshCommandFlow = listRefreshCommand,
    )
    UstadLazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        println("doorRepoPager${doorRepoPager.lazyPagingItems.itemSnapshotList}")
        ustadPagedItems(
            pagingItems = doorRepoPager.lazyPagingItems,
            key = { it.reportUid },
        ) { reportAndDetails ->
            Row (modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                ListItem(
                    modifier = Modifier.weight(0.3f)
                        .clickable {
                            reportAndDetails?.also { onListItemClick(it) }
                        },
                    headlineContent = {
                        Text(
                            text = reportAndDetails?.reportTitle.toString() ?: ""
                        )
                    },
                )
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove filter",
                    modifier = Modifier
                        .defaultScreenPadding()
                        .clickable {
                            onRemoveReport(reportAndDetails?.reportUid ?: 0)
                        }
                )
            }
        }
    }
}