package com.ustadmobile.libuicompose.view.coursegroupset.list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import app.cash.paging.compose.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ListItem
import androidx.compose.runtime.collectAsState
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListUiState
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListViewModel
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.libuicompose.components.UstadAddListItem
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.libuicompose.components.ustadPagedItems
import androidx.compose.runtime.remember
import androidx.paging.compose.collectAsLazyPagingItems
import com.ustadmobile.core.util.SortOrderOption

@Suppress("unused") // Pending
@Composable
fun CourseGroupSetListScreen(
    viewModel: CourseGroupSetListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(CourseGroupSetListUiState())

    CourseGroupSetListScreen(
        uiState = uiState,
        onClickEntry = viewModel::onClickEntry,
        onClickNewItem = viewModel::onClickAdd,
        onSortOrderChanged = viewModel::onSortOptionChanged
    )
}

@Composable
fun CourseGroupSetListScreen(
    uiState: CourseGroupSetListUiState,
    onClickEntry: (CourseGroupSet) -> Unit = {},
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onClickNewItem: () -> Unit = {},
) {

    val pager = remember(uiState.courseGroupSets) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.courseGroupSets,
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(
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

        uiState.individualSubmissionOption?.also { individualOption ->
            item(key = "individualsubmission") {
                ListItem(
                    modifier = Modifier.clickable {
                        individualOption.also(onClickEntry)
                    },
                    headlineContent = {
                        Text(individualOption.cgsName ?: "")
                    },
                )
            }
        }

        if(uiState.showAddItem) {
            item(key = "additem") {
                UstadAddListItem(
                    text = stringResource(MR.strings.add_new_groups),
                    onClickAdd = onClickNewItem,
                )
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