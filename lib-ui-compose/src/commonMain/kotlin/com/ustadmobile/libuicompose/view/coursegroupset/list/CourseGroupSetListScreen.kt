package com.ustadmobile.libuicompose.view.coursegroupset.list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import app.cash.paging.compose.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ListItem
import androidx.compose.runtime.collectAsState
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListUiState
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListViewModel
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.libuicompose.components.UstadAddListItem
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding

@Composable
fun CourseGroupSetListScreenForViewModel(
    viewModel: CourseGroupSetListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(CourseGroupSetListUiState())
//    val context = LocalContext.current

    CourseGroupSetListScreen(
        uiState = uiState,
        onClickEntry = viewModel::onClickEntry,
        onClickNewItem = viewModel::onClickAdd,
        onClickSort =  {
                       // TODO error
//            SortBottomSheetFragment(
//                sortOptions = uiState.sortOptions,
//                selectedSort = uiState.sortOption,
//                onSortOptionSelected = {
//                    viewModel.onSortOptionChanged(it)
//                }
//            ).show(context.getContextSupportFragmentManager(), "SortOptions")
        },
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseGroupSetListScreen(
    uiState: CourseGroupSetListUiState,
    onClickEntry: (CourseGroupSet) -> Unit = {},
    onClickSort: () -> Unit = {},
    onClickNewItem: () -> Unit = {},
) {

    // TODO error
//    val pager = remember {
//        Pager(
//            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
//            pagingSourceFactory = uiState.courseGroupSets,
//        )
//    }
//
//    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){
        item(key = "sortheader") {
            UstadListSortHeader(
                modifier = Modifier.defaultItemPadding(),
                activeSortOrderOption = uiState.sortOption,
                onClick =   onClickSort
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

//        items(
//            items = lazyPagingItems,
//            key = { it.cgsUid }
//        ) { courseGroupSet ->
//            ListItem(
//                modifier = Modifier.clickable {
//                    courseGroupSet?.also(onClickEntry)
//                },
//                headlineContent = {
//                    Text(courseGroupSet?.cgsName ?: "")
//                },
//            )
//        }
    }
}