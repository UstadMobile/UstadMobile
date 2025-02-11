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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListViewModel
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.FilterOption
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.FilterType
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadListSortHeader
import com.ustadmobile.libuicompose.components.UstadNothingHereYet
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import com.ustadmobile.libuicompose.util.rememberFormattedDuration
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.Flow

@Composable
fun ContentEntryDetailAttemptsStatementListScreen(
    viewModel: ContentEntryDetailAttemptsStatementListViewModel
) {
    val uiState = viewModel.uiState.collectAsState(ContentEntryDetailAttemptsStatementListUiState())
    ContentEntryDetailAttemptsStatementList(
        uiState = uiState.value,
        refreshCommandFlow = viewModel.refreshCommandFlow,
        onSortOrderChanged = viewModel::onSortOrderChanged,
        onFilterChanged = { filterOption -> viewModel.onFilterChanged(filterOption) },
    )
}

@Composable
fun ContentEntryDetailAttemptsStatementList(
    uiState: ContentEntryDetailAttemptsStatementListUiState,
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onFilterChanged: (FilterOption) -> Unit = { },
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
                    onClickSortOption =  onSortOrderChanged,
                )
            }
        }

        item("row") {
            FilterRow(
                filterOptions = listOf(
                    FilterOption(
                        labelResId = MR.strings.experience,
                        isSelected = uiState.isExperienceSelected,
                        filterType = FilterType.EXPERIENCE
                    ),
                    FilterOption(
                        labelResId = MR.strings.answered,
                        isSelected = uiState.isAnsweredSelected,
                        filterType = FilterType.ANSWERED
                    ),
                    FilterOption(
                        labelResId = MR.strings.failed,
                        isSelected = uiState.isFailedSelected,
                        filterType = FilterType.FAILED
                    ),
                    FilterOption(
                        labelResId = MR.strings.completed,
                        isSelected = uiState.isCompletedSelected,
                        filterType = FilterType.COMPLETED
                    )
                ),
                onFilterChanged = onFilterChanged
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

            val formattedDuration =
                attemptsStatementListItems?.statementEntity?.resultDuration?.let {
                    rememberFormattedDuration(
                        timeInMillis = it,
                    )
                }

            val progress = statementEntity?.extensionProgress?.takeIf { it > 0 }?.div(100f)
                ?: statementEntity?.let { entity ->
                    val raw = entity.resultScoreRaw ?: 0f
                    val max = entity.resultScoreMax?.takeIf { it > 0 }
                        ?: 100f
                    (raw / max).coerceIn(0f, 1f)
                } ?: 0f
            val rawScore = statementEntity?.resultScoreRaw
            val maxScore = statementEntity?.resultScoreMax

            val scoreText = if (rawScore != null && maxScore != null) {
                "$score: ${rawScore.toInt()} / ${maxScore.toInt()}"
            } else {
                "$percentageCompletion: ${statementEntity?.extensionProgress ?: 0}%"
            }

            androidx.compose.material3.ListItem(
                modifier = Modifier.clickable {
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Work,
                        contentDescription = "Icon",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                headlineContent = {
                    Text(
                        text = attemptsStatementListItems?.verb?.verbUrlId.toString()
                            .substringAfterLast("/").replaceFirstChar { it.uppercaseChar() }
                    )
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
                                modifier = Modifier
                                    .padding(end = 8.dp)
                            )

                            Text(
                                text = formattedDuration ?: "N/A",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier.weight(0.7f),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = scoreText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .width(48.dp),
                                textAlign = TextAlign.Center
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
    filterOptions: List<FilterOption>,
    onFilterChanged: (FilterOption) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .defaultItemPadding(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filterOptions.forEach { filterOption ->
            FilterChip(
                selected = filterOption.isSelected,
                onClick = {
                    onFilterChanged(filterOption.copy(isSelected = !filterOption.isSelected))
                },
                label = {
                    Text(
                        text = stringResource(filterOption.labelResId),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}