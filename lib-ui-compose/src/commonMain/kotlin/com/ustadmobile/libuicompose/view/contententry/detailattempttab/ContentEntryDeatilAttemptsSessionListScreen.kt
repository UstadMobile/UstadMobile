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
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListViewModel
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.flow.Flow

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
            androidx.compose.material3.ListItem(
                modifier = Modifier.clickable {
                    attemptsSessionListItems?.also(onClickEntry)
                },
                leadingContent = {
                    // Choose the appropriate icon based on the condition
                    Icon(
                        imageVector =
                          Icons.Filled.Close
                        ,
                        contentDescription =
                          "Icon"
                        ,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                headlineContent = {
                    androidx.compose.material3.Text(
                        text =
                       " ${attemptsSessionListItems?.maxScore}",


                    )
                },
                supportingContent = {
                    androidx.compose.material3.Text(
                        text =
                        " ${attemptsSessionListItems?.maxProgress}",


                        )

                }

            )

       /*     androidx.compose.material3.ListItem(
                modifier = Modifier.clickable {
                    attemptsSessionListItems?.also(onClickEntry)
                },
                leadingContent = {
                    // Choose the appropriate icon based on the condition
                    Icon(
                        imageVector = when {
                            // If extensionProgress is null, check resultSuccess
                            attemptsSessionListItems?.statement?.extensionProgress == null -> {
                                if (attemptsSessionListItems?.statement?.resultSuccess == true) Icons.Filled.Check
                                else Icons.Filled.Close
                            }
                            // If extensionProgress is not null, check its value
                            attemptsSessionListItems?.statement?.extensionProgress == 100 -> Icons.Filled.Check
                            else -> Icons.Filled.Close
                        },
                        contentDescription = when {
                            // If extensionProgress is null, check resultSuccess
                            attemptsSessionListItems?.statement?.extensionProgress == null -> {
                                if (attemptsSessionListItems?.statement?.resultSuccess == true) "Success"
                                else "Failure"
                            }
                            // If extensionProgress is 100, set as Completed
                            attemptsSessionListItems?.statement?.extensionProgress == 100 -> "Completed"
                            else -> "Incomplete"
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                headlineContent = {
                    androidx.compose.material3.Text(
                        text = when {
                            attemptsSessionListItems?.statement?.extensionProgress == null -> {
                                // If extensionProgress is  null, check resultSuccess
                                when {
                                    attemptsSessionListItems?.statement?.resultSuccess == true -> "Passed"
                                    attemptsSessionListItems?.statement?.resultSuccess == false -> "Failed"
                                    else -> "" // Default if resultSuccess is null
                                }
                            }
                            else -> {

                                if (attemptsSessionListItems?.statement?.extensionProgress == 100) {
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
                            verticalAlignment = Alignment.CenterVertically // Align vertically in the center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star, // You can replace this with your desired icon
                                contentDescription = "Icon",
                                modifier = Modifier.padding(8.dp) // Add space between the icon and the text
                            )
                            Text(
                                text = when {
                                    attemptsSessionListItems?.statement?.extensionProgress == null -> {
                                        // If extensionProgress is null, show the score text
                                        "${(attemptsSessionListItems?.statement?.resultScoreRaw)?.toInt()}/${(attemptsSessionListItems?.statement?.resultScoreMax)?.toInt()} Score"
                                    }
                                    else -> {
                                        // If extensionProgress is not null, show the completion text
                                        "${(attemptsSessionListItems.statement?.extensionProgress)}% Completion"
                                    }
                                }
                            )

                        }
                    }

                }

            )*/
        }
    }
}


