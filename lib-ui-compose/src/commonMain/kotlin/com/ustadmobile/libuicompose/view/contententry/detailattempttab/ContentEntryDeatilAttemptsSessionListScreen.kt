package com.ustadmobile.libuicompose.view.contententry.detailattempttab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListUiState
import com.ustadmobile.lib.db.PersonAndSessionInfo
import com.ustadmobile.lib.db.composites.PersonAndAttemptInfo
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KFunction1

@Composable
fun ContentEntryDetailAttemptsSessionListScreen(
    viewModel: ContentEntryDetailAttemptsSessionListViewModel
) {
    // Collect the personName from the ViewModel
    val uiState = viewModel.uiState.collectAsState(ContentEntryDetailAttemptsSessionListUiState())

    // Display the person's name or a fallback if the name is empty
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
    onClickEntry: (StatementEntity) -> Unit = {},
) {
    val attemptsSessionListPager =
        rememberDoorRepositoryPager(uiState.attemptsSessionList, refreshCommandFlow)
    val attemptsSessionListItems = attemptsSessionListPager.lazyPagingItems

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ){
        ustadPagedItems(
            pagingItems = attemptsSessionListItems,
            key = {it.statementIdHi }
        )  { attemptsSessionListItems ->
            androidx.compose.material3.ListItem(
                modifier = Modifier.clickable {
                    attemptsSessionListItems?.also(onClickEntry)
                },
                leadingContent = {
                    // Add an icon in the leading content
                    Icon(
                        imageVector = Icons.Filled.Check, // Tick (check) icon
                        contentDescription = "Icon", // Description for accessibility
                        modifier = Modifier.padding(end = 8.dp) // Add padding between icon and text
                    )
                },
                headlineContent = {
                    androidx.compose.material3.Text(
                        text = if (attemptsSessionListItems?.resultCompletion == true) {
                            "Completed"
                        } else {
                            "Incomplete"
                        }
                    )
                },

                        supportingContent = {
                    // Supporting text
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "date",
                        )
                        // Additional text below supporting text
                        Text(
                            text = "100%",
                            modifier = Modifier.padding(top = 4.dp) // Add spacing between the texts
                        )
                    }
                }
            )
        }
    }
}


