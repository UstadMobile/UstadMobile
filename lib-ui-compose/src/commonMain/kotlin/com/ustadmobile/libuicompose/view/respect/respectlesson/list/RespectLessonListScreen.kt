package com.ustadmobile.libuicompose.view.respect.respectlesson.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.respect.respectlesson.list.RespectLessonListUiState
import com.ustadmobile.core.viewmodel.respect.respectlesson.list.RespectLessonListViewModel
import com.ustadmobile.lib.db.entities.respect.RespectLessonAndApp
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun RespectLessonListScreen(
    viewModel: RespectLessonListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(RespectLessonListUiState())

    RespectLessonListScreen(
        uiState = uiState,
        onClick = viewModel::onClickItem,
    )
}


@Composable
fun RespectLessonListScreen(
    uiState: RespectLessonListUiState,
    listRefreshCommand: Flow<RefreshCommand> = emptyFlow(),
    onClick: (RespectLessonAndApp) -> Unit = { },
) {

    val doorRepoPager = rememberDoorRepositoryPager(
        pagingSourceFactory = uiState.lessons,
        refreshCommandFlow = listRefreshCommand,
    )

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        ustadPagedItems(
            pagingItems = doorRepoPager.lazyPagingItems,
            key = { it.lesson.rlUid }
        ) {  item ->
            ListItem(
                modifier = Modifier.clickable {
                    item?.also(onClick)
                },
                headlineContent = {
                    Text(item?.lesson?.rlTitle ?: "")
                }
            )
        }
    }

}
