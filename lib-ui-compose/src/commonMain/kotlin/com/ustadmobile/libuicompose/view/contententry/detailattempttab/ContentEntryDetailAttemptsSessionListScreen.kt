package com.ustadmobile.libuicompose.view.contententry.detailattempttab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListViewModel
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.descriptionStringRes
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.statementSummary
import com.ustadmobile.lib.db.composites.xapi.SessionTimeAndProgressInfo
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.components.UstadNothingHereYet
import com.ustadmobile.libuicompose.components.UstadProgressBarWithLabel
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone

@Composable
fun ContentEntryDetailAttemptsSessionListScreen(
    viewModel: ContentEntryDetailAttemptsSessionListViewModel
) {
    val uiState = viewModel.uiState.collectAsState(ContentEntryDetailAttemptsSessionListUiState())

    ContentEntryDetailAttemptsSessionListScreen(
        uiState = uiState.value,
        refreshCommandFlow = viewModel.refreshCommandFlow,
        onClickEntry = viewModel::onClickEntry,
        onSortOrderChanged = viewModel::onSortOrderChanged
    )
}

@Composable
fun ContentEntryDetailAttemptsSessionListScreen(
    uiState: ContentEntryDetailAttemptsSessionListUiState,
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onClickEntry: (SessionTimeAndProgressInfo) -> Unit = {},
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
) {
    val attemptsSessionListPager = rememberDoorRepositoryPager(
        pagingSourceFactory = uiState.attemptsSessionList,
        refreshCommandFlow = refreshCommandFlow
    )

    val attemptsSessionListItems = attemptsSessionListPager.lazyPagingItems

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.showSortOptions) {
            item("sort_options") {
                UstadListSortHeader(
                    modifier = Modifier
                        .defaultItemPadding()
                        .fillMaxWidth(),
                    activeSortOrderOption = uiState.sortOption,
                    sortOptions = uiState.sortOptions,
                    onClickSortOption = onSortOrderChanged,
                )
            }
        }

        if (attemptsSessionListPager.isSettledEmpty) {
            item("empty_state") {
                UstadNothingHereYet()
            }
        }

        ustadPagedItems(
            pagingItems = attemptsSessionListItems,
            key = { Pair(it.contextRegistrationHi, it.contextRegistrationLo)}
        ) { attemptsSessionListItem ->
            val formattedDateAndTime = attemptsSessionListItem?.let {
                rememberFormattedDateTime(
                    timeInMillis = it.timeStarted,
                    timeZoneId = TimeZone.currentSystemDefault().id,
                )
            }

            ListItem(
                modifier = Modifier.clickable {
                    attemptsSessionListItem?.also(onClickEntry)
                },
                leadingContent = {
                    Icon(
                        imageVector = when {
                            attemptsSessionListItem?.isSuccessful == true -> Icons.Filled.Star
                            attemptsSessionListItem?.isSuccessful == false -> Icons.Filled.Cancel
                            attemptsSessionListItem?.isCompleted == true -> Icons.Filled.Check
                            else -> Icons.Filled.Pending
                        },
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(
                        attemptsSessionListItem?.statementSummary?.descriptionStringRes?.let {
                            stringResource(it)
                        } ?: ""
                    )
                },
                supportingContent = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        formattedDateAndTime?.also {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = null,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(it)
                            }
                        }

                        attemptsSessionListItem?.maxProgress?.also {
                            UstadProgressBarWithLabel(
                                labelContent = {
                                    Text(stringResource(MR.strings.progress_key).capitalizeFirstLetter())
                                },
                                progress = { it.toFloat() / 100f },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        attemptsSessionListItem?.maxScore?.also {
                            UstadProgressBarWithLabel(
                                labelContent = {
                                    Text(stringResource(MR.strings.content_score).capitalizeFirstLetter())
                                },
                                progress = { it },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            )
        }
    }
}