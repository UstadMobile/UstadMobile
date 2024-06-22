package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.ext.roundTo
import com.ustadmobile.core.viewmodel.clazz.progressreport.ClazzGradebookUiState
import com.ustadmobile.core.viewmodel.clazz.progressreport.ClazzGradebookViewModel
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
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

private val NAME_WIDTH = 240

private val COLUMN_WIDTH = 72

private val COLUMN_HEIGHT = 72

private val HEADER_HEIGHT = 140

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


    val horizontalScrollState = rememberScrollState()

    ClazzProgressReportLazyColumn(
        horizontalScrollState = horizontalScrollState,
        lazyListState = rememberLazyListState(),
        stickyHeight = HEADER_HEIGHT.dp,
        stickyWidth = NAME_WIDTH.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .height(HEADER_HEIGHT.dp)
            ) {
                Spacer(Modifier.width(NAME_WIDTH.dp))

                Row(
                    modifier = Modifier
                        .weight(1.0f)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    uiState.courseBlocks.forEach { block ->
                        GradebookCourseBlockHeader(
                            headline = block.cbTitle ?: "",
                            subtitle = block.cbMaxPoints?.let { "/$it" },
                            width = COLUMN_WIDTH.dp,
                            height = HEADER_HEIGHT.dp
                        )
                    }
                }
            }
        }

        items(
            count = listResult.lazyPagingItems.itemCount,
            key = { index ->
                listResult.lazyPagingItems.get(index)?.student?.person?.personUid ?: index.toLong()
            }
        ) { row ->
            val rowItem = listResult.lazyPagingItems[row]
            Row(
                modifier = Modifier.fillMaxWidth()
                    .height(COLUMN_HEIGHT.dp)
                    .defaultItemPadding()
            ) {
                Row(
                    modifier = Modifier.width(NAME_WIDTH.dp)
                ) {
                    UstadPersonAvatar(
                        pictureUri = rowItem?.student?.personPicture?.personPictureThumbnailUri,
                        personName = rowItem?.student?.person?.fullName(),
                    )

                    Spacer(Modifier.width(16.dp))

                    Text(
                        text = rowItem?.student?.person?.fullName() ?: "-",
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    uiState.courseBlocks.forEach { block ->
                        val result = rowItem?.blockStatuses?.firstOrNull {
                            it.sCbUid == block.cbUid
                        }

                        val scoreScaled = result?.sScoreScaled
                        val maxPoints = block.cbMaxPoints
                        val mark = if(scoreScaled != null && maxPoints != null) {
                            scoreScaled * maxPoints
                        }else {
                            null
                        }

                        Text(
                            text = mark?.roundTo(2)?.toString() ?: "-",
                            modifier = Modifier.width(COLUMN_WIDTH.dp),
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}