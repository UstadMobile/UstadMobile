package com.ustadmobile.libuicompose.view.contententry.detailattempttab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListUiState
import com.ustadmobile.lib.db.composites.StatementAndPersonAndPicture
import com.ustadmobile.lib.db.composites.xapi.SessionTimeAndProgressInfo
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
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

        )
}

@Composable
fun ContentEntryDetailAttemptsSessionListScreen(
    uiState: ContentEntryDetailAttemptsSessionListUiState,
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onClickEntry: (SessionTimeAndProgressInfo) -> Unit = {},
) {
    val attemptsSessionListPager =
        rememberDoorRepositoryPager(uiState.attemptsSessionList, refreshCommandFlow)
    val attemptsSessionListItems = attemptsSessionListPager.lazyPagingItems

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        ustadPagedItems(
            pagingItems = attemptsSessionListItems,
            key = { it.contextRegistrationHi.toInt() ?: -1 }
        ) { attemptsSessionListItems ->
            val timeZoneId = remember { TimeZone.currentSystemDefault().id }

            val duration = attemptsSessionListItems?.let {
                rememberFormattedDateTime(
                    timeInMillis = it.timeStarted,
                    timeZoneId = timeZoneId,
                    joinDateAndTime = {date, time -> "$date - $time"},
                    )
            }

            androidx.compose.material3.ListItem(
                modifier = Modifier.clickable {
                    attemptsSessionListItems?.also(onClickEntry)
                },
                leadingContent = {
                    Icon(
                        imageVector = when {
                            attemptsSessionListItems?.isSuccessful != null -> {
                                if (attemptsSessionListItems?.isSuccessful == true) Icons.Filled.Check
                                else Icons.Filled.Close
                            }

                            attemptsSessionListItems?.isCompleted == true -> Icons.Filled.Check
                            else -> Icons.Filled.Close
                        },
                        contentDescription = when {
                            attemptsSessionListItems?.isSuccessful != null -> {
                                if (attemptsSessionListItems?.isSuccessful == true) "Success"
                                else "Failure"
                            }

                            attemptsSessionListItems?.isCompleted == true -> "Completed"
                            else -> "Incomplete"
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                headlineContent = {
                    androidx.compose.material3.Text(
                        text = when {
                            attemptsSessionListItems?.isSuccessful != null -> {
                                when {
                                    attemptsSessionListItems?.isSuccessful == true -> "Passed"
                                    attemptsSessionListItems?.isSuccessful == false -> "Failed"
                                    else -> "" // Default if resultSuccess is null
                                }
                            }

                            else -> {

                                if (attemptsSessionListItems?.isCompleted == true) {
                                    "Completed"
                                } else {
                                    "Incomplete"
                                }
                            }
                        }

                    )
                },
                supportingContent = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = "Icon",
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(
                                text =
                                        "$duration"

                            )

                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Icon",
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(
                                text = when {
                                    attemptsSessionListItems?.maxScore != null -> {
                                        "${((attemptsSessionListItems?.maxScore ?: 0f) * 100).toInt()}% Score"
                                    }

                                    else -> {
                                        "${(attemptsSessionListItems?.maxProgress)}% Completion"
                                    }
                                }
                            )

                        }


                    }

                }

            )
        }
    }
}


