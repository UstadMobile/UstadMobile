package com.ustadmobile.libuicompose.view.contententry.detailattempttab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListViewModel
import com.ustadmobile.lib.db.composites.PersonAndPictureAndNumAttempts
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.components.UstadNothingHereYet
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import dev.icerock.moko.resources.compose.stringResource
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
        onSortOrderChanged = viewModel::onSortOrderChanged,
        )
}

@Composable
fun ContentEntryDetailAttemptsPersonListScreen(
    uiState: ContentEntryDetailAttemptsPersonListUiState,
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onClickEntry: (PersonAndPictureAndNumAttempts) -> Unit = {},
    onSortOrderChanged: (SortOrderOption) -> Unit = { },

    ) {
    val attemptsPersonListPager =
        rememberDoorRepositoryPager(uiState.attemptsPersonList, refreshCommandFlow)

    val attemptsPersonListItems = attemptsPersonListPager.lazyPagingItems
    val attempts = stringResource(MR.strings.attempts)
    val percentageCompletion = stringResource(MR.strings.content_percentage_completion)
    val percentageScore = stringResource(MR.strings.content_score)

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
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
        if (attemptsPersonListPager.isSettledEmpty) {
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
                    Text(text = attemptsPersonListItems?.person?.fullName() ?: "")
                },
                supportingContent = {
                    Text(text = "${attemptsPersonListItems?.numAttempts.toString()} $attempts")
                },
                leadingContent = {
                    UstadPersonAvatar(
                        pictureUri = attemptsPersonListItems?.picture?.personPictureThumbnailUri,
                        personName = attemptsPersonListItems?.person?.fullName(),
                    )
                }
            )
            if (attemptsPersonListItems?.maxScore != null || attemptsPersonListItems?.maxProgress != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    if ((attemptsPersonListItems.maxProgress ?: 0) > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = (attemptsPersonListItems?.maxProgress?.toFloat()
                                    ?: 0f) / 100f,
                                modifier = Modifier.weight(0.7f).padding(start = 12.dp).testTag("progress_bar"),
                            )

                            Text(
                                text = "${(attemptsPersonListItems?.maxProgress ?: 0)}% $percentageCompletion",
                                modifier = Modifier.padding(start = 8.dp).weight(0.3f),
                            )
                        }
                    }
                    if (attemptsPersonListItems.maxScore != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = attemptsPersonListItems.maxScore!!.toFloat(),
                                modifier = Modifier
                                    .weight(0.7f)
                                    .padding(start = 12.dp).testTag("progress_bar"),
                            )

                            Text(
                                text = "${(attemptsPersonListItems.maxScore!!.toFloat() * 100).toInt()}% $percentageScore",
                                modifier = Modifier.padding(start = 8.dp).weight(0.3f),
                            )
                        }
                    }
                }
            }
        }
    }
}