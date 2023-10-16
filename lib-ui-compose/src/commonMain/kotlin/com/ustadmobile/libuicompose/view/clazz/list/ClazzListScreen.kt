package com.ustadmobile.libuicompose.view.clazz.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.impl.locale.entityconstants.RoleConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListUiState
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadListFilterChipsHeader
import com.ustadmobile.libuicompose.components.UstadListSortHeader

@Composable
fun ClazzListScreenForViewModel(viewModel: ClazzListViewModel) {
    val uiState: ClazzListUiState by viewModel.uiState.collectAsState(initial = ClazzListUiState())

//    val context = LocalContext.current

    ClazzListScreen(
        uiState = uiState,
        onClickClazz = viewModel::onClickEntry,
        onClickSort =  {
            //  TODO error
//            SortBottomSheetFragment(
//                sortOptions = uiState.sortOptions,
//                selectedSort = uiState.activeSortOrderOption,
//                onSortOptionSelected = {
//                    viewModel.onSortOrderChanged(it)
//                }
//            ).show(context.getContextSupportFragmentManager(), "SortOptions")
        },
        onClickFilterChip = viewModel::onClickFilterChip
    )
}

@Composable
fun ClazzListScreen(
    uiState: ClazzListUiState = ClazzListUiState(),
    onClickClazz: (Clazz) -> Unit = {},
    onClickSort: () -> Unit = {},
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
) {

    //  TODO error
//    val pager = remember(uiState.clazzList){
//        Pager(
//            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
//            pagingSourceFactory = uiState.clazzList
//        )
//    }

    //  TODO error
//    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),

        // 600 width of the smallest iPad,
        // subtracted 16 = horizontal padding & space between cards,
        // half of 584 is 292
        // card width = 292dp.
        columns = GridCells.Adaptive(292.dp)
    ) {

        item(span = { GridItemSpan(maxLineSpan) }) {
            //  TODO error
            UstadListSortHeader(
                                   //  TODO error
//                modifier = Modifier.defaultItemPadding(),
                activeSortOrderOption = uiState.activeSortOrderOption,
                enabled = uiState.fieldsEnabled,
                onClickSort = onClickSort
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            UstadListFilterChipsHeader(
                                   //  TODO error
//                modifier = Modifier.defaultItemPadding(),
                filterOptions = uiState.filterOptions,
                selectedChipId = uiState.selectedChipId,
                enabled = uiState.fieldsEnabled,
                onClickFilterChip = onClickFilterChip,
            )
        }


        /**
         * Note: Currently there is no direct support for LazyGrid with pagingsource.
         */


        /**
         * Note: Currently there is no direct support for LazyGrid with pagingsource.
         */
//        items(
//            lazyPagingItems.itemCount
//        ) {
//            ClazzListItem(
//                clazz = lazyPagingItems[it],
//                onClickClazz = onClickClazz
//            )
//        }

        //Host fragment thinks scroll bar behavior increases available height - need to compensate
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(176.dp))
        }
    }
}

@Composable
fun ClazzListItem(
    clazz: ClazzWithListDisplayDetails?,
    onClickClazz: (Clazz) -> Unit
){

    val role = RoleConstants.ROLE_MESSAGE_IDS.find {
        it.value == clazz?.clazzActiveEnrolment?.clazzEnrolmentRole
    }?.stringResource

    Card(
        modifier = Modifier
            //  TODO error
//            .defaultItemPadding()
            .clickable {
                clazz?.also { onClickClazz(it) }
            },
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = clazz?.clazzName ?: "",
                        style = MaterialTheme.typography.h6,
                        maxLines = 1,
                    )
                    //  TODO error
//                    HtmlText(
//                        html = clazz?.clazzDesc ?: "",
//                        htmlMaxLines = 2,
//                    )
                }

//                if(role != null) {
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Filled.Badge,
//                            contentDescription = "",
//                        )
//                        Text(messageIdResource(id = role))
//                    }
//                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Filled.People,
                    contentDescription = "",
                )
                Text(
                    text = stringResource(
                        MR.strings.x_teachers_y_students,
                        clazz?.numTeachers ?: 0, clazz?.numStudents ?: 0,
                    )
                )
            }
        }
    }
}