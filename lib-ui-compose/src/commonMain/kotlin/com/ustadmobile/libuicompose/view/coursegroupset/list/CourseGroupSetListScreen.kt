package com.ustadmobile.libuicompose.view.coursegroupset.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListUiState
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListViewModel
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.libuicompose.components.UstadAddListItem
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.components.UstadNothingHereYet
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.Flow

@Suppress("unused") // Pending
@Composable
fun CourseGroupSetListScreen(
    viewModel: CourseGroupSetListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(CourseGroupSetListUiState())

    CourseGroupSetListScreen(
        uiState = uiState,
        refreshCommandFlow = viewModel.refreshCommandFlow,
        onClickEntry = viewModel::onClickEntry,
        onClickNewItem = viewModel::onClickAdd,
        onSortOrderChanged = viewModel::onSortOptionChanged
    )
}

@Composable
fun CourseGroupSetListScreen(
    uiState: CourseGroupSetListUiState,
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onClickEntry: (CourseGroupSet) -> Unit = {},
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onClickNewItem: () -> Unit = {},
) {

    val repositoryResult = rememberDoorRepositoryPager(
        uiState.courseGroupSets, refreshCommandFlow
    )

    val lazyPagingItems = repositoryResult.lazyPagingItems

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ){
        item(key = "sortheader") {
            UstadListSortHeader(
                modifier = Modifier.defaultItemPadding(),
                activeSortOrderOption = uiState.sortOption,
                sortOptions = uiState.sortOptions,
                onClickSortOption =  onSortOrderChanged,
            )
        }

        if(uiState.showAddItem) {
            item(key = "additem") {
                UstadAddListItem(
                    text = stringResource(MR.strings.add_new_groups),
                    onClickAdd = onClickNewItem,
                )
            }
        }

        if(repositoryResult.isSettledEmpty){
            item("empty_state") {
                UstadNothingHereYet()
            }
        }

        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { it.cgsUid }
        ) { courseGroupSet ->
            ListItem(
                modifier = Modifier.clickable {
                    courseGroupSet?.also(onClickEntry)
                },
                headlineContent = {
                    Text(courseGroupSet?.cgsName ?: "")
                },
            )
        }
    }
}