package com.ustadmobile.libuicompose.view.contententry.detailattempttab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListViewModel
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.descriptionStringRes
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.statementSummary
import com.ustadmobile.lib.db.composites.PersonAndPictureAndNumAttempts
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.components.UstadNothingHereYet
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.UstadProgressBarWithLabel
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
    val percentageScoreLabel = stringResource(MR.strings.content_score).capitalizeFirstLetter()
    val progressLabel = stringResource(MR.strings.progress_key).capitalizeFirstLetter()

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        if(uiState.showSortOptions) {
            item("sort_options") {
                UstadListSortHeader(
                    modifier = Modifier.defaultItemPadding().fillMaxWidth(),
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
                    Text((attemptsPersonListItems?.person?.fullName() ?: "") +
                            ": ${attemptsPersonListItems?.statementSummary?.descriptionStringRes
                                ?.let { stringResource(it) } ?: ""}",
                        maxLines = 1)
                },
                supportingContent = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("${attemptsPersonListItems?.numAttempts.toString()} $attempts")

                        attemptsPersonListItems?.maxProgress?.also { maxProgressVal ->
                            UstadProgressBarWithLabel(
                                progress = { (maxProgressVal.toFloat()) / 100f },
                                labelContent = { Text(progressLabel) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        attemptsPersonListItems?.maxScore?.also { maxScoreVal ->
                            UstadProgressBarWithLabel(
                                progress = { maxScoreVal },
                                labelContent = { Text(percentageScoreLabel) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
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