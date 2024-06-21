package com.ustadmobile.libuicompose.view.clazz.progressreport

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.clazz.progressreport.ClazzProgressReportUiState
import com.ustadmobile.core.viewmodel.clazz.progressreport.ClazzProgressReportViewModel
import com.ustadmobile.libuicompose.components.UstadHorizontalScrollbar
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import kotlinx.coroutines.flow.Flow

@Composable
fun ClazzProgressReportScreen(
    viewModel: ClazzProgressReportViewModel,
) {
    val uiState by viewModel.uiState.collectAsState(ClazzProgressReportUiState())
    ClazzProgressReportScreen(
        uiState = uiState,
        refreshCommandFlow = viewModel.refreshCommandFlow
    )
}

//Might be possible to use stickyHeader and
// https://developer.android.com/develop/ui/compose/touch-input/pointer-input/scroll
// e.g. consume the delta, do not apply it to the sticky section
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClazzProgressReportScreen(
    uiState: ClazzProgressReportUiState,
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
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        LazyColumn(
            modifier = Modifier.weight(1.0f).fillMaxWidth()
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

        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(colWidth.dp))
            UstadHorizontalScrollbar(
                scrollState = horizontalScrollState,
                modifier = Modifier.weight(1.0f).height(8.dp)
            )
        }
    }
}