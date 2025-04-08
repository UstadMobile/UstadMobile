package com.ustadmobile.libuicompose.view.respect.respectassignment.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.respect.respectassignment.list.RespectAssignmentListUiState
import com.ustadmobile.core.viewmodel.respect.respectassignment.list.RespectAssignmentListViewModel
import com.ustadmobile.lib.db.entities.respect.RespectAssignment
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun RespectAssignmentListScreen(
    viewModel: RespectAssignmentListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(RespectAssignmentListUiState())

    RespectAssignmentListScreen(
        uiState = uiState,
        onClickAssignment = viewModel::onClickItem
    )
}

@Composable
fun RespectAssignmentListScreen(
    uiState: RespectAssignmentListUiState,
    listRefreshCommand: Flow<RefreshCommand> = emptyFlow(),
    onClickAssignment: (RespectAssignment) -> Unit,
) {
    val doorRepoPager = rememberDoorRepositoryPager(
        pagingSourceFactory = uiState.assignments,
        refreshCommandFlow = listRefreshCommand,
    )

    UstadLazyColumn(modifier = Modifier.fillMaxWidth()) {
        ustadPagedItems(
            pagingItems = doorRepoPager.lazyPagingItems,
            key = { it.razUid }
        ) { item ->
            ListItem(
                modifier = Modifier.clickable {
                    item?.also(onClickAssignment)
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(item?.razTitle ?: "")
                },
                supportingContent = {
                    Text(item?.razDescription ?: "")
                }
            )
        }
    }
}
