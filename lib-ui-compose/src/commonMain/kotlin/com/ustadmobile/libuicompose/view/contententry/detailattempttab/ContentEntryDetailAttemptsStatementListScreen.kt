package com.ustadmobile.libuicompose.view.contententry.detailattempttab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListViewModel
import com.ustadmobile.lib.db.entities.xapi.VerbEntity
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.components.UstadNothingHereYet
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone

@Composable
fun ContentEntryDetailAttemptsStatementListScreen(
    viewModel: ContentEntryDetailAttemptsStatementListViewModel
) {
    val uiState = viewModel.uiState.collectAsState(ContentEntryDetailAttemptsStatementListUiState())
    ContentEntryDetailAttemptsStatementList(
        uiState = uiState.value,
        refreshCommandFlow = viewModel.refreshCommandFlow,
        onSortOrderChanged = viewModel::onSortOrderChanged,
        onVerbFilterToggled = viewModel::onVerbFilterToggled,
    )
}

@Composable
fun ContentEntryDetailAttemptsStatementList(
    uiState: ContentEntryDetailAttemptsStatementListUiState,
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onVerbFilterToggled: (String) -> Unit = { },
) {
    val attemptsStatementListPager =
        rememberDoorRepositoryPager(uiState.attemptsStatementList, refreshCommandFlow)
    val attemptsStatementListItems = attemptsStatementListPager.lazyPagingItems
    val percentageCompletion = stringResource(MR.strings.content_percentage_completion)
    val score = stringResource(MR.strings.content_score)

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
                    onClickSortOption = onSortOrderChanged,
                )
            }
        }

        item("verb_filters") {
            FilterRow(
                availableVerbs = uiState.availableVerbs,
                selectedVerbIds = uiState.selectedVerbIds,
                onVerbFilterToggled = onVerbFilterToggled
            )
        }

        if(attemptsStatementListPager.isSettledEmpty) {
            item("empty_state") {
                UstadNothingHereYet()
            }
        }

        ustadPagedItems(
            pagingItems = attemptsStatementListItems,
            key = { it.statementEntity?.statementIdHi ?: -1 }
        ) { attemptsStatementListItems ->
            val statementEntity = attemptsStatementListItems?.statementEntity

            val formattedTimestamp = statementEntity?.timestamp?.let {
                rememberFormattedDateTime(
                    timeInMillis = it,
                    timeZoneId = TimeZone.currentSystemDefault().id,
                    joinDateAndTime = { date, time ->
                        "$date, $time"
                    }
                )
            } ?: "N/A"

            val progress = statementEntity?.extensionProgress?.takeIf { it > 0 }?.div(100f)
                ?: statementEntity?.let { entity ->
                    val raw = entity.resultScoreRaw ?: 0f
                    val max = entity.resultScoreMax?.takeIf { it > 0 } ?: 100f
                    (raw / max).coerceIn(0f, 1f)
                } ?: 0f

            val rawScore = statementEntity?.resultScoreRaw
            val maxScore = statementEntity?.resultScoreMax
            val progressPercentage = (progress * 100).toInt()

            val scoreText = if (rawScore != null && maxScore != null) {
                // Calculate percentage score if rawScore and maxScore are available
                val percentageScore = (rawScore / maxScore) * 100
                "${percentageScore.toInt()}% $score"
            } else {
                // Show progress as percentage
                "${progressPercentage}% $percentageCompletion"
            }


            androidx.compose.material3.ListItem(
                modifier = Modifier.clickable { },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Work,
                        contentDescription = "Icon",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                headlineContent = {
                    attemptsStatementListItems?.verb?.verbUrlId?.let { verbId ->
                        Text(
                            text = verbId.substringAfterLast("/").replaceFirstChar { it.uppercaseChar() }
                        )
                    } ?: Text(text = "N/A")
                },
                supportingContent = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = formattedTimestamp ?: "N/A",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically // Align vertically in the center
                        ) {
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .weight(0.7f) // Occupy 70% of the width
                                    .testTag("progress_bar"),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = scoreText,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .weight(0.3f),
                            )
                        }

                    }
                }
            )
        }
    }
}

@Composable
fun FilterRow(
    availableVerbs: List<VerbEntity>,
    selectedVerbIds: List<Long>,
    onVerbFilterToggled: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .defaultItemPadding(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        availableVerbs
            .distinctBy { it.verbUrlId }
            .forEach { verb ->
                verb.verbUrlId?.let { verbId ->
                    val verbName = verbId.substringAfterLast("/")
                        .replaceFirstChar { it.uppercase() }

                    key(verbId) {
                        FilterChip(
                            selected = verb.verbUid in selectedVerbIds,
                            onClick = { onVerbFilterToggled(verbId) },
                            label = {
                                Text(
                                    text = verbName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = if (verb.verbUid in selectedVerbIds) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = verb.verbUid in selectedVerbIds
                            )
                        )
                    }
                }
            }
    }
}