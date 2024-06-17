package com.ustadmobile.libuicompose.view.clazz.progressreport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.clazz.progressreport.ClazzProgressReportUiState
import com.ustadmobile.core.viewmodel.clazz.progressreport.ClazzProgressReportViewModel
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import eu.wewox.lazytable.LazyTable
import eu.wewox.lazytable.LazyTableItem
import eu.wewox.lazytable.lazyTableDimensions
import eu.wewox.lazytable.lazyTablePinConfiguration
import eu.wewox.lazytable.rememberLazyTableState
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


    val state = rememberLazyTableState()


    LazyTable(
        modifier = Modifier.fillMaxSize(),
        dimensions = lazyTableDimensions(
            columnsSize = listOf(150.dp) + (0 until uiState.courseBlocks.size).map { 80.dp },
            rowsSize = (0 until rows).map { 40.dp },
        ),
        state = state,
        pinConfiguration = lazyTablePinConfiguration(
            columns = 1, rows = 1,
        )
    ){
        items(
            count = rows * columns,
            layoutInfo = { index ->
                LazyTableItem(
                    column = index % columns,
                    row = index / columns,
                )
            }
        ) { index ->
            val column = index % columns
            val row = index / columns

            val courseBlockIndex = column - 1

            when {
                //Top start spacer - do nothing
                row == 0 && column == 0 -> {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(MaterialTheme.colors.background),
                    ) {
                        //nothing
                    }
                }

                //Course Block header
                row == 0 && column > 0 -> {
                    Text(uiState.courseBlocks.getOrNull(courseBlockIndex)?.cbTitle ?: "Null")
                }

                //Student name, pic, etc.
                column == 0 -> {
                    val studentItem = listResult.lazyPagingItems.get(row - 1)
                    Text(studentItem?.student?.person?.fullName() ?: "Null person")
                }

                //Student result for given course block item.
                else -> {
                    Text("Cat")
                }
            }
        }
    }
}