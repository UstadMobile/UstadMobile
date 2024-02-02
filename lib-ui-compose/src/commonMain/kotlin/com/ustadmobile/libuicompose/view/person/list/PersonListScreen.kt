package com.ustadmobile.libuicompose.view.person.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.core.viewmodel.person.list.PersonListUiState
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.libuicompose.components.UstadAddListItem
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding

@Suppress("unused") // Pending
@Composable
fun PersonListScreen(
    viewModel: PersonListViewModel
) {
    val uiState: PersonListUiState by viewModel.uiState.collectAsState(PersonListUiState())

    PersonListScreen(
        uiState = uiState,
        onListItemClick = viewModel::onClickEntry,
        onClickAddNew = viewModel::onClickAdd,
        onSortOrderChanged = viewModel::onSortOrderChanged
    )
}

@Composable
fun PersonListScreen(
    uiState: PersonListUiState,
    onListItemClick: (Person) -> Unit = {},
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onClickAddNew: () -> Unit = {},
){

    // As per
    // https://developer.android.com/reference/kotlin/androidx/paging/compose/package-summary#collectaslazypagingitems
    // Must provide a factory to pagingSourceFactory that will
    // https://issuetracker.google.com/issues/241124061
    val pager = remember(uiState.personList) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.personList
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    UstadLazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){

        item {
            UstadListSortHeader(
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxWidth(),
                activeSortOrderOption = uiState.sortOption,
                sortOptions = uiState.sortOptions,
                onClickSortOption =  onSortOrderChanged,
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

        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { it.person?.personUid ?: 0 },
        ) {  personAndDetails ->
            ListItem(
                modifier = Modifier.clickable {
                    personAndDetails?.person?.also { onListItemClick(it) }
                },
                headlineContent = { Text(text = personAndDetails?.person?.fullName() ?: "") },
                leadingContent = {
                    UstadPersonAvatar(
                        personName = personAndDetails?.person?.fullName(),
                        pictureUri = personAndDetails?.picture?.personPictureUri,
                    )
                },
            )
        }
    }
}