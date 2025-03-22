package com.ustadmobile.libuicompose.view.contententry.detailattempttab

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.xapi.formatresponse.FormatStatementResponseUseCase
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.util.ext.displayName
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListViewModel
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.verbDisplayName
import com.ustadmobile.lib.db.composites.xapi.StatementEntityAndVerb
import com.ustadmobile.lib.db.entities.xapi.VerbEntity
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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.TimeZone

@Composable
fun ContentEntryDetailAttemptsStatementListScreen(
    viewModel: ContentEntryDetailAttemptsStatementListViewModel
) {
    val uiState = viewModel.uiState.collectAsState(ContentEntryDetailAttemptsStatementListUiState())
    ContentEntryDetailAttemptsStatementList(
        uiState = uiState.value,
        formattedResponseFlow = viewModel::formattedStatementResponse,
        refreshCommandFlow = viewModel.refreshCommandFlow,
        onSortOrderChanged = viewModel::onSortOrderChanged,
        onVerbFilterToggled = viewModel::onVerbFilterToggled,
    )
}

@Composable
fun ContentEntryDetailAttemptsStatementList(
    uiState: ContentEntryDetailAttemptsStatementListUiState,
    formattedResponseFlow: (StatementEntityAndVerb) -> Flow<FormatStatementResponseUseCase.FormattedStatementResponse>,
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onVerbFilterToggled: (VerbEntity) -> Unit = { },
) {
    val attemptsStatementListPager =
        rememberDoorRepositoryPager(uiState.attemptsStatementList, refreshCommandFlow)
    val attemptsStatementListItems = attemptsStatementListPager.lazyPagingItems

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                uiState.availableVerbs.forEach { verb ->
                    key(verb.verbEntity.verbUid) {
                        FilterChip(
                            selected = verb.verbEntity.verbUid !in uiState.deselectedVerbUids,
                            onClick = { onVerbFilterToggled(verb.verbEntity) },
                            label = {
                                Text(verb.displayName().capitalizeFirstLetter())
                            },
                            leadingIcon = if (verb.verbEntity.verbUid !in uiState.deselectedVerbUids) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null,
                        )
                    }
                }
            }
        }

        if(attemptsStatementListPager.isSettledEmpty) {
            item("empty_state") {
                UstadNothingHereYet()
            }
        }

        ustadPagedItems(
            pagingItems = attemptsStatementListItems,
            key = { Pair(it.statementEntity.statementIdHi, it.statementEntity.statementIdLo) }
        ) { item ->
            val statementEntity = item?.statementEntity

            val formattedTimestamp = statementEntity?.timestamp?.let {
                rememberFormattedDateTime(
                    timeInMillis = it,
                    timeZoneId = TimeZone.currentSystemDefault().id,
                )
            } ?: ""

            val formattedResponseFlowVal = remember(
                item?.statementEntity?.statementIdHi, item?.statementEntity?.statementIdLo
            ) {
                item?.let { formattedResponseFlow(it) } ?: emptyFlow()
            }

            val formattedResponse by formattedResponseFlowVal.collectAsState(
                initial = FormatStatementResponseUseCase.FormattedStatementResponse("")
            )

            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Work,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    val activityName = item?.activityLangMapEntry?.almeValue ?: ""
                    Text(
                        "${item?.verbDisplayName?.capitalizeFirstLetter() ?: ""} $activityName",
                    )
                },
                supportingContent = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        item?.statementActivityDescription?.also {
                            Text(it.trim())
                        }

                        formattedResponse.string?.takeIf { it.isNotEmpty() }?.also {
                            Text("${stringResource(MR.strings.response)}: ${it.trim()}")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = formattedTimestamp)
                        }

                        item?.statementEntity?.extensionProgress?.also { progressVal ->
                            UstadProgressBarWithLabel(
                                labelContent = {
                                    Text(stringResource(MR.strings.progress_key).capitalizeFirstLetter())
                                },
                                progress = { progressVal.toFloat() / 100f },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        item?.statementEntity?.resultScoreScaled?.also { scoreScaledVal ->
                            UstadProgressBarWithLabel(
                                labelContent = {
                                    Text(stringResource(MR.strings.content_score).capitalizeFirstLetter())
                                },
                                progress = { scoreScaledVal },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            )
        }
    }
}
