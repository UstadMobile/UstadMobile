package com.ustadmobile.libuicompose.view.contententry.detailattempttab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListViewModel
import com.ustadmobile.lib.db.composites.PersonAndPictureAndNumAttempts
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadNothingHereYet
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
    onClickEntry: (PersonAndPictureAndNumAttempts) -> Unit = {},
    ) {
    val attemptsPersonListPager =
        rememberDoorRepositoryPager(uiState.attemptsPersonList, refreshCommandFlow)
    val attemptsPersonListItems = attemptsPersonListPager.lazyPagingItems
    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        if(attemptsPersonListPager.isSettledEmpty) {
            item("empty_state") {
                UstadNothingHereYet()
            }
        }
        ustadPagedItems(
            pagingItems = attemptsPersonListItems,
            key = { it.person.personUid }
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
                    androidx.compose.material3.Text(text = "${attemptsPersonListItems?.numAttempts.toString()} attempts")
                },
                leadingContent = {
                    UstadPersonAvatar(
                        pictureUri = attemptsPersonListItems?.picture?.personPictureThumbnailUri,
                        personName = attemptsPersonListItems?.person?.fullName(),
                    )
                }
            )
            if (attemptsPersonListItems?.maxScore != null || attemptsPersonListItems?.maxProgress != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val progressValue = attemptsPersonListItems.maxProgress?.toFloat()
                        ?: attemptsPersonListItems.maxScore ?: 0f
                    LinearProgressIndicator(
                        progress = progressValue,
                        modifier = Modifier.weight(0.7f).padding(start = 12.dp),
                    )
                    Text(
                        text = when {
                            attemptsPersonListItems.maxProgress != null ->
                                "${(attemptsPersonListItems.maxProgress ?: 0f)}% Completion"

                            else -> "${((attemptsPersonListItems.maxScore ?: 0f) * 100).toInt()}% Score"
                        },
                        modifier = Modifier.padding(start = 8.dp).weight(0.3f),
                    )
                }
            }
        }
    }
}
