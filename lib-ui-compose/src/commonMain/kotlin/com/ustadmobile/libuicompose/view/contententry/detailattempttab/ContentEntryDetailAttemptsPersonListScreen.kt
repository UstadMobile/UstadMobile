package com.ustadmobile.libuicompose.view.contententry.detailattempttab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListViewModel
import com.ustadmobile.lib.db.composites.PersonAndAttemptInfo
import com.ustadmobile.lib.db.composites.StatementAndPersonAndPicture
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import kotlinx.coroutines.flow.Flow


@Composable
fun ContentEntryDetailAttemptsPersonListScreen(
    viewModel: ContentEntryDetailAttemptsPersonListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ContentEntryDetailAttemptsPersonListUiState())

    ContentEntryDetailAttemptsPersonListScreen(
        uiState = uiState,
        refreshCommandFlow = viewModel.refreshCommandFlow,
        onClickEntry = viewModel::onClickEntry,
        )
}

@Composable
fun ContentEntryDetailAttemptsPersonListScreen(
    uiState: ContentEntryDetailAttemptsPersonListUiState,
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onClickEntry: (StatementAndPersonAndPicture) -> Unit = {},
    ) {
    val attemptsPersonListPager =
        rememberDoorRepositoryPager(uiState.attemptsPersonList, refreshCommandFlow)
    val attemptsPersonListItems = attemptsPersonListPager.lazyPagingItems
    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        ustadPagedItems(
            pagingItems = attemptsPersonListItems,
            key = { it.person?.personUid ?: -1 }
        ) { attemptsPersonListItems ->
            ListItem(
                modifier = Modifier.clickable {
                    attemptsPersonListItems?.also(onClickEntry)
                },
                headlineContent = {
                    androidx.compose.material3.Text(
                        text = attemptsPersonListItems?.person?.fullName() ?: ""
                    )
                },
                supportingContent = {
                    androidx.compose.material3.Text(text = "")
                },
                leadingContent = {
                    UstadPersonAvatar(
                        pictureUri = attemptsPersonListItems?.picture?.personPictureThumbnailUri,
                        personName = attemptsPersonListItems?.person?.fullName(),
                    )
                }
            )
        }
    }
}
