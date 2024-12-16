import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListViewModel
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsStatementListUiState
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import kotlinx.coroutines.flow.Flow

@Composable
fun ContentEntryDetailAttemptsStatementListScreen(
    viewModel: ContentEntryDetailAttemptsStatementListViewModel
) {
    val uiState = viewModel.uiState.collectAsState(ContentEntryDetailAttemptsStatementListUiState())
    ContentEntryDetailAttemptsStatementListScreen(
        uiState = uiState.value,
        refreshCommandFlow = viewModel.refreshCommandFlow,

        )
}

@Composable
fun ContentEntryDetailAttemptsStatementListScreen(
    uiState: ContentEntryDetailAttemptsStatementListUiState,
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
) {
    val attemptsStatementListPager =
        rememberDoorRepositoryPager(uiState.attemptsStatementList, refreshCommandFlow)
    val attemptsStatementListItems = attemptsStatementListPager.lazyPagingItems

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        ustadPagedItems(
            pagingItems = attemptsStatementListItems,
            key = { it.statementEntity?.statementIdHi ?: -1 }
        ) { attemptsStatementListItems ->


            androidx.compose.material3.ListItem(
                modifier = Modifier.clickable {
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Icon",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                headlineContent = {
                    androidx.compose.material3.Text(
                        text = attemptsStatementListItems?.verb?.verbUrlId.toString()
                            .substringAfterLast("/").replaceFirstChar { it.uppercaseChar() }
                    )
                },

                supportingContent = {
                    Column(modifier = Modifier.fillMaxWidth()) {
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
                                text = "${attemptsStatementListItems?.statementEntity?.timestamp}"
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
                                text = if (attemptsStatementListItems?.statementEntity?.extensionProgress == null) {
                                    if (attemptsStatementListItems?.statementEntity?.resultScoreRaw != null) {
                                        "${
                                            attemptsStatementListItems?.statementEntity?.resultScoreRaw?.toInt()
                                                .toString()
                                        }/${
                                            attemptsStatementListItems?.statementEntity?.resultScoreMax?.toInt()
                                                .toString()
                                        } Score"
                                    } else {
                                        "No Score"
                                    }

                                } else {
                                    "${attemptsStatementListItems?.statementEntity?.extensionProgress} %Completion"
                                }
                            )

                        }
                    }
                }
            )
        }
    }
}


