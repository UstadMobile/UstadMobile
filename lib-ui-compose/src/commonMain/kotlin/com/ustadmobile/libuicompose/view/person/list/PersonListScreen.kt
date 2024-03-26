package com.ustadmobile.libuicompose.view.person.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.paging.ExperimentalPagingApi
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
import com.ustadmobile.door.paging.DoorRepositoryRemoteMediator
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.libuicompose.components.UstadBottomSheetOption
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("unused") // Pending
@Composable
fun PersonListScreen(
    viewModel: PersonListViewModel
) {
    val uiState: PersonListUiState by viewModel.uiState.collectAsState(PersonListUiState())

    PersonListScreen(
        uiState = uiState,
        listRefreshCommand = viewModel.listRefreshCommandFlow,
        onListItemClick = viewModel::onClickEntry,
        onClickAddNew = viewModel::onClickAdd,
        onSortOrderChanged = viewModel::onSortOrderChanged,
        onClickInviteWithLink = viewModel::onClickInviteWithLink,
        onClickCopyInviteCode = viewModel::onClickCopyInviteCode,
    )

    if(uiState.addSheetOrDialogVisible) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissAddSheetOrDialog
        ) {
            UstadBottomSheetOption(
                modifier = Modifier.clickable {
                    viewModel.onDismissAddSheetOrDialog()
                    viewModel.onClickAdd()
                },
                headlineContent = {
                    Text(stringResource(MR.strings.add_person))
                },
                leadingContent = {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                }
            )

            UstadBottomSheetOption(
                modifier = Modifier.clickable {
                    viewModel.onDismissAddSheetOrDialog()
                    viewModel.onClickBulkAdd()
                },
                headlineContent = {
                    Text(stringResource(MR.strings.bulk_import))
                },
                leadingContent = {
                    Icon(Icons.Default.GroupAdd, contentDescription = null)
                }
            )
        }
    }

}

@OptIn(ExperimentalPagingApi::class)
@Composable
fun PersonListScreen(
    uiState: PersonListUiState,
    listRefreshCommand: Flow<Boolean> = emptyFlow(),
    onListItemClick: (Person) -> Unit = {},
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onClickAddNew: () -> Unit = {},
    onClickInviteWithLink: () -> Unit = {},
    onClickCopyInviteCode: () -> Unit = { },
){

    // As per
    // https://developer.android.com/reference/kotlin/androidx/paging/compose/package-summary#collectaslazypagingitems
    // Must provide a factory to pagingSourceFactory that will
    // https://issuetracker.google.com/issues/241124061
    val pager = remember(uiState.personList) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.personList,
            remoteMediator = DoorRepositoryRemoteMediator(uiState.personList),
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LaunchedEffect(listRefreshCommand) {
        listRefreshCommand.collect {
            lazyPagingItems.refresh()
        }
    }

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ){
        if(uiState.showSortOptions) {
            item("sort_options") {
                UstadListSortHeader(
                    modifier = Modifier
                        .defaultItemPadding()
                        .fillMaxWidth(),
                    activeSortOrderOption = uiState.sortOption,
                    sortOptions = uiState.sortOptions,
                    onClickSortOption =  onSortOrderChanged,
                )
            }
        }

        uiState.inviteCode?.also { inviteCode ->
            item("copy_invite_code") {
                ListItem(
                    modifier = Modifier.testTag("copy_invite_code")
                        .clickable { onClickCopyInviteCode() },
                    headlineContent = { Text(stringResource(MR.strings.copy_invite_code) +": $inviteCode") },
                    leadingContent = { Icon(Icons.Default.CopyAll, contentDescription = null)}
                )
            }
        }

        if(uiState.showInviteViaLink) {
            item("invite_with_link") {
                ListItem(
                    modifier = Modifier
                        .testTag("invite_with_link")
                        .clickable { onClickInviteWithLink() },
                    headlineContent = { Text(stringResource(MR.strings.invite_with_link)) },
                    leadingContent = { Icon(Icons.Default.Link, contentDescription = null) },
                )
            }
        }

        if(uiState.showAddItem) {
            item("add_new_person") {
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