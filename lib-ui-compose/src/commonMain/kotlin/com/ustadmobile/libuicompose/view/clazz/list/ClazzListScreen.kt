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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.core.impl.locale.entityconstants.RoleConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListUiState
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.libuicompose.components.HtmlText
import com.ustadmobile.libuicompose.components.UstadBottomSheetOption
import com.ustadmobile.libuicompose.components.UstadBottomSheetSpacer
import com.ustadmobile.libuicompose.components.UstadListFilterChipsHeader
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.nav.UstadNavControllerPreCompose
import com.ustadmobile.libuicompose.util.ext.copyWithNewFabOnClick
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.viewmodel.ustadViewModel
import moe.tlaster.precompose.navigation.BackStackEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClazzListScreen(
    backStackEntry: BackStackEntry,
    navController: UstadNavControllerPreCompose,
    onSetAppUiState: (AppUiState) -> Unit,
    destName: String,
) {
    var createNewOptionsVisible by remember {
        mutableStateOf(false)
    }

    val viewModel = ustadViewModel(
        modelClass = ClazzListViewModel::class,
        backStackEntry = backStackEntry,
        onSetAppUiState = onSetAppUiState,
        navController = navController,
        appUiStateMap = {
            it.copyWithNewFabOnClick {
                createNewOptionsVisible = true
            }
        }
    ) { di, savedStateHandle ->
        ClazzListViewModel(di, savedStateHandle, destName)
    }

    val uiState: ClazzListUiState by viewModel.uiState.collectAsState(initial = ClazzListUiState())


    if(createNewOptionsVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                createNewOptionsVisible = false
            }
        ) {
            UstadBottomSheetOption(
                modifier = Modifier.clickable {
                    createNewOptionsVisible = false
                    viewModel.onClickJoinExistingClazz()
                },
                headlineContent = { Text(stringResource(MR.strings.join_existing_course)) },
                leadingContent = { Icon(Icons.Default.Login, contentDescription = null) },
            )

            if(uiState.canAddNewCourse) {
                UstadBottomSheetOption(
                    modifier = Modifier.clickable {
                        createNewOptionsVisible = false
                        viewModel.onClickAdd()
                    },
                    headlineContent = { Text(stringResource(MR.strings.add_a_new_course)) },
                    leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                )
            }

            UstadBottomSheetSpacer()
        }
    }


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

    val pager = remember(uiState.clazzList){
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.clazzList
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),

        // 600 width of the smallest iPad,
        // subtracted 16 = horizontal padding & space between cards,
        // half of 584 is 292
        // card width = 292dp.
        columns = GridCells.Adaptive(292.dp)
    ) {

        item(span = { GridItemSpan(maxLineSpan) }) {
            UstadListSortHeader(
                modifier = Modifier.defaultItemPadding(),
                activeSortOrderOption = uiState.activeSortOrderOption,
                enabled = uiState.fieldsEnabled,
                onClickSort = onClickSort
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            UstadListFilterChipsHeader(
                modifier = Modifier.defaultItemPadding(),
                filterOptions = uiState.filterOptions,
                selectedChipId = uiState.selectedChipId,
                enabled = uiState.fieldsEnabled,
                onClickFilterChip = onClickFilterChip,
            )
        }

        /**
         * Note: Currently there is no direct support for LazyGrid with pagingsource.
         */
        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { it.clazzUid }
        ){
            ClazzListItem(
                clazz = it,
                onClickClazz = onClickClazz
            )
        }

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
            .defaultItemPadding()
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
                    HtmlText(
                        html = clazz?.clazzDesc ?: "",
                        htmlMaxLines = 2,
                    )
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