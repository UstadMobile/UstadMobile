package com.ustadmobile.libuicompose.view.contententry.detailattempttab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListViewModel
import com.ustadmobile.lib.db.composites.xapi.SessionTimeAndProgressInfo
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.components.UstadNothingHereYet
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import com.ustadmobile.libuicompose.util.rememberFormattedDuration
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
    val attemptsSessionListPager =
        rememberDoorRepositoryPager(
            pagingSourceFactory = { uiState.attemptsSessionList() },
            refreshCommandFlow = refreshCommandFlow
        )

    val attemptsSessionListItems = attemptsSessionListPager.lazyPagingItems

    val percentageCompletion = stringResource(MR.strings.content_percentage_completion)
    val percentageScore = stringResource(MR.strings.content_score)
    val passed = stringResource(MR.strings.passed)
    val failed = stringResource(MR.strings.failed)
    val completed = stringResource(MR.strings.completed)
    val incomplete = stringResource(MR.strings.incomplete)



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
            key = { it.contextRegistrationHi.toInt() }) { attemptsSessionListItem ->

            val timeZoneId = remember { TimeZone.currentSystemDefault().id }
            val formattedDuration = attemptsSessionListItem?.resultDuration?.let {
                rememberFormattedDuration(timeInMillis = it)
            }

            val formattedDateAndTime = attemptsSessionListItem?.let {
                rememberFormattedDateTime(
                    timeInMillis = it.timeStarted,
                    timeZoneId = timeZoneId,
                    joinDateAndTime = { date, time -> "$date - $time" },
                )
            }



            val statusText = when {
                attemptsSessionListItem?.isSuccessful == true -> {
                    if (formattedDuration != null) "$passed - $formattedDuration" else "$passed"
                }
                attemptsSessionListItem?.isSuccessful == false -> {
                    if (formattedDuration != null) "$failed - $formattedDuration" else "$failed"
                }
                attemptsSessionListItem?.isCompleted == true -> completed
                else -> incomplete
            }

            androidx.compose.material3.ListItem(
                modifier = Modifier.clickable {
                    attemptsSessionListItem?.also(onClickEntry)
                },
                leadingContent = {
                    Icon(
                        imageVector = when {
                            attemptsSessionListItem?.isSuccessful == true -> Icons.Filled.Star // Star for passed
                            attemptsSessionListItem?.isSuccessful == false -> Icons.Filled.Cancel // Cross for failed
                            else -> Icons.Filled.Check // Check for completed
                        },
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                headlineContent = {
                    Text(text = "$statusText")
                },
                supportingContent = {
                    Column {
                        if (formattedDateAndTime != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically // Ensure both elements align at center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = null,
                                )
                                Text(
                                    text = formattedDateAndTime,
                                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 8.dp) // Ensure text aligns with the icon
                                )
                            }

                        }

                        if (attemptsSessionListItem?.maxScore != null || attemptsSessionListItem?.maxProgress != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val progressValue = when {
                                    attemptsSessionListItem?.maxProgress != null -> {
                                        (attemptsSessionListItem.maxProgress!!.toFloat() / 100f).coerceIn(0f, 1f)
                                    }
                                    attemptsSessionListItem?.maxScore != null -> {
                                        attemptsSessionListItem.maxScore!!.toFloat().coerceIn(0f, 1f)
                                    }
                                    else -> 0f
                                }

                                LinearProgressIndicator(
                                    progress = progressValue,
                                    modifier = Modifier.weight(0.7f).testTag("progress_bar")


                                )
                                Text(
                                    text = when {
                                        attemptsSessionListItem?.maxScore != null ->
                                            "${(attemptsSessionListItem.maxScore!! * 100).toInt()}% $percentageScore"
                                        attemptsSessionListItem?.maxProgress != null ->
                                            "${attemptsSessionListItem.maxProgress}% $percentageCompletion"
                                        else -> "0% $percentageCompletion"
                                    },
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .weight(0.3f),
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}