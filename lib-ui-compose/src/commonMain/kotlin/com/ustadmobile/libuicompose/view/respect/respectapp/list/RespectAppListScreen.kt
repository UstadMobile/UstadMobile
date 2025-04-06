package com.ustadmobile.libuicompose.view.respect.respectapp.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.respect.respectapp.list.RespectAppListUiState
import com.ustadmobile.core.viewmodel.respect.respectapp.list.RespectAppListViewModel
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.libuicompose.components.UstadLazyVerticalGrid
import com.ustadmobile.libuicompose.components.ustadPagedItems

@Composable
fun RespectAppListScreen(
    viewModel: RespectAppListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(RespectAppListUiState())

    RespectAppListScreen(uiState)
}

@Composable
fun RespectAppListScreen(
    uiState: RespectAppListUiState,
    listRefreshCommand: Flow<RefreshCommand> = emptyFlow(),
) {


    val doorRepoPager = rememberDoorRepositoryPager(
        pagingSourceFactory = uiState.appList,
        refreshCommandFlow = listRefreshCommand,
    )

    UstadLazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),

        // 600 width of the smallest iPad,
        // subtracted 16 = horizontal padding & space between cards,
        // half of 584 is 292
        // card width = 292dp.
        columns = GridCells.Adaptive(146.dp)
    ) {
        ustadPagedItems(
            pagingItems = doorRepoPager.lazyPagingItems,
            key = { it.raUid }
        ) { item ->
            RespectAppCard(
                respectApp = item,
                onClick = {

                },
            )
        }
    }

}





