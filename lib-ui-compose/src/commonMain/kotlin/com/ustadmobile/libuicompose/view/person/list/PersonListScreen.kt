package com.ustadmobile.libuicompose.view.person.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.person.list.PersonListUiState
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.libuicompose.components.UstadAddListItem
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding

@Suppress("unused") // Pending
@Composable
fun PersonListScreenForViewModel(
    viewModel: PersonListViewModel
) {
    val uiState: PersonListUiState by viewModel.uiState.collectAsState(PersonListUiState())

//    val context = LocalContext.current

    PersonListScreen(
        uiState = uiState,
        onListItemClick = viewModel::onClickEntry,
        onClickAddNew = viewModel::onClickAdd,
    )
}

@Composable
fun PersonListScreen(
    uiState: PersonListUiState,
    onListItemClick: (PersonWithDisplayDetails) -> Unit = {},
    onClickAddNew: () -> Unit = {},
){

    // As per
    // https://developer.android.com/reference/kotlin/androidx/paging/compose/package-summary#collectaslazypagingitems
    // Must provide a factory to pagingSourceFactory that will
    // https://issuetracker.google.com/issues/241124061
    //  TODO error
//    val pager = remember(uiState.personList) {
//        Pager(
//            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
//            pagingSourceFactory = uiState.personList
//        )
//    }

    //  TODO error
//    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){

        item {
            UstadListSortHeader(
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxWidth(),
                activeSortOrderOption = uiState.sortOption
            )
        }

        if(uiState.showAddItem) {
            item {
                UstadAddListItem(
                    modifier = Modifier.testTag("add_new_person"),
                    text = stringResource(MR.strings.add_a_new_person),
                    onClickAdd = onClickAddNew
                )
            }
        }

        //  TODO error
//        items(
//            items = lazyPagingItems,
//            key = { it.personUid },
//        ) {  person ->
//            ListItem(
//                modifier = Modifier
//                    .clickable {
//                        person?.also { onListItemClick(it) }
//                    },
//                text = { Text(text = "${person?.firstNames} ${person?.lastName}") },
//                icon = {
//                    //  TODO error
////                    UstadPersonAvatar(
////                        person?.personUid ?: 0,
////                        modifier = Modifier.defaultAvatarSize(),
////                    )
//                },
//            )
//        }

    }
}