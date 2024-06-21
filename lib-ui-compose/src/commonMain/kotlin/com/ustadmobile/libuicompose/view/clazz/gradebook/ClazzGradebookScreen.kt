package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.clazz.progressreport.ClazzGradebookUiState
import com.ustadmobile.core.viewmodel.clazz.progressreport.ClazzGradebookViewModel
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import kotlinx.coroutines.flow.Flow

@Composable
fun ClazzGradebookScreen(
    viewModel: ClazzGradebookViewModel,
) {
    val uiState by viewModel.uiState.collectAsState(ClazzGradebookUiState())
    ClazzGradebookScreen(
        uiState = uiState,
        refreshCommandFlow = viewModel.refreshCommandFlow
    )
}

//Might be possible to use stickyHeader and
// https://developer.android.com/develop/ui/compose/touch-input/pointer-input/scroll
// e.g. consume the delta, do not apply it to the sticky section
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClazzGradebookScreen(
    uiState: ClazzGradebookUiState,
    refreshCommandFlow: Flow<RefreshCommand>
) {
    val listResult = rememberDoorRepositoryPager(
        pagingSourceFactory = uiState.results,
        refreshCommandFlow = refreshCommandFlow
    )

    val rows = listResult.lazyPagingItems.itemCount + 1
    val columns = uiState.courseBlocks.size + 1

    val numcols = 20
    val colWidth = 80
    val width = (numcols * colWidth)

    val horizontalScrollState = rememberScrollState()

    ClazzProgressReportLazyColumn(
        horizontalScrollState = horizontalScrollState,
        lazyListState = rememberLazyListState(),
        stickyHeight = 20.dp,
        stickyWidth = colWidth.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
            ) {
                Spacer(Modifier.width(colWidth.dp))

                Row(
                    modifier = Modifier
                        .weight(1.0f)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    (0..numcols).forEach { col ->
                        Text("C $col", modifier = Modifier.width(colWidth.dp))
                    }
                }
            }
        }

        items(count = 100) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Row $row", modifier = Modifier.width(colWidth.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    (0..numcols).forEach { col ->
                        Text("$row-$col", modifier = Modifier.width(colWidth.dp))
                    }
                }
            }
        }
    }
}