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
    onClickEntry: (StatementAndPersonAndPicture) -> Unit = {},
) {
    val attemptsSessionListPager =
        rememberDoorRepositoryPager(uiState.attemptsSessionList, refreshCommandFlow)
    val attemptsSessionListItems = attemptsSessionListPager.lazyPagingItems

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        ustadPagedItems(
            pagingItems = attemptsSessionListItems,
            key = { it.person?.personUid ?: -1 }
        ) { attemptsSessionListItems ->
            val resultScore = attemptsSessionListItems?.statement?.resultScoreScaled ?: 0f
            val percentage = (resultScore * 100).toInt()
            val resultDuration = attemptsSessionListItems?.statement?.resultDuration?.toInt()?:0
            val minutes = resultDuration / 60
            val seconds = resultDuration % 60

            androidx.compose.material3.ListItem(
                modifier = Modifier.clickable {
                    attemptsSessionListItems?.also(onClickEntry)
                },
                leadingContent = {
                    Icon(
                        imageVector = if (attemptsSessionListItems?.statement?.resultSuccess==true) Icons.Filled.Star else Icons.Filled.Close,
                        contentDescription = if (attemptsSessionListItems?.statement?.resultSuccess==true) "Success" else "Failure",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                headlineContent = {
                    androidx.compose.material3.Text(
                        text = when {
                            attemptsSessionListItems?.statement?.resultSuccess==true -> "Passed"
                            attemptsSessionListItems?.statement?.resultSuccess==false-> "Failed"
                            else -> ""
                        },
                    )
                },
                supportingContent = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically // Align vertically in the center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check, // You can replace this with your desired icon
                                contentDescription = "Completion Icon",
                                modifier = Modifier.padding(8.dp) // Add space between the icon and the text
                            )
                            Text(text = "${((attemptsSessionListItems?.statement?.resultScoreScaled ?: 0f) * 100).toInt()}% Completion",

                                )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically // Align vertically in the center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star, // You can replace this with your desired icon
                                contentDescription = "Score Icon",
                                modifier = Modifier.padding(8.dp) // Add space between the icon and the text
                            )
                            Text(text = "${(attemptsSessionListItems?.statement?.resultScoreRaw)?.toInt()}/${(attemptsSessionListItems?.statement?.resultScoreMax)?.toInt()} Score",

                                )
                        }
                    }

                }

            )
        }
    }
}


