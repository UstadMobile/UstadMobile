package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.ext.roundTo
import com.ustadmobile.core.viewmodel.clazz.gradebook.ClazzGradebookUiState
import com.ustadmobile.core.viewmodel.clazz.gradebook.ClazzGradebookViewModel
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import kotlinx.coroutines.flow.Flow


@Composable
fun ClazzGradebookScreen(
    viewModel: ClazzGradebookViewModel,
) {
    val uiState by viewModel.uiState.collectAsState(ClazzGradebookUiState())
    ClazzGradebookScreen(
        uiState = uiState,
        refreshCommandFlow = viewModel.refreshCommandFlow,
        onClickFullScreen = viewModel::onClickFullScreen,
        onClickZoomIn =  viewModel::onClickZoomIn,
        onClickZoomOut = viewModel::onClickZoomOut,
        onToggleZoom = viewModel::onToggleZoom,
    )
}

private val NAME_WIDTH = 240

private val COLUMN_WIDTH = 72

private val HEADER_HEIGHT = 192

//Might be possible to use stickyHeader and
// https://developer.android.com/develop/ui/compose/touch-input/pointer-input/scroll
// e.g. consume the delta, do not apply it to the sticky section
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClazzGradebookScreen(
    uiState: ClazzGradebookUiState,
    refreshCommandFlow: Flow<RefreshCommand>,
    onClickFullScreen: () -> Unit,
    onClickZoomIn: () -> Unit,
    onClickZoomOut: () -> Unit,
    onToggleZoom: () -> Unit,
) {
    val listResult = rememberDoorRepositoryPager(
        pagingSourceFactory = uiState.results,
        refreshCommandFlow = refreshCommandFlow
    )

    val horizontalScrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            onToggleZoom()
                        }
                    )
                }
    ) {
        val nameColWidth = minOf(maxWidth / 2, NAME_WIDTH.dp)
        val headerHeight = minOf(maxHeight / 2, HEADER_HEIGHT.dp)

        ClazzGradebookLazyColumn(
            horizontalScrollState = horizontalScrollState,
            lazyListState = rememberLazyListState(),
            stickyHeight = headerHeight,
            stickyWidth = nameColWidth,
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            scale = uiState.scale,
            modifier = Modifier.fillMaxSize()
        ) {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxWidth()
                        .height(headerHeight)
                ) {
                    Spacer(Modifier.width(nameColWidth))

                    Row(
                        modifier = Modifier
                            .weight(1.0f)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        uiState.courseBlocks.forEach { block ->
                            GradebookCourseBlockHeader(
                                courseBlock = block,
                                width = COLUMN_WIDTH.dp,
                                height = headerHeight
                            )
                        }
                    }
                }
            }

            items(
                count = listResult.lazyPagingItems.itemCount,
                key = { index ->
                    listResult.lazyPagingItems[index]?.student?.person?.personUid ?: index.toLong()
                }
            ) { row ->
                val rowItem = listResult.lazyPagingItems[row]
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        modifier = Modifier.width(nameColWidth),
                        headlineContent = {
                            Text(
                                text = rowItem?.student?.person?.fullName() ?: "-",
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        leadingContent = {
                            UstadPersonAvatar(
                                pictureUri = rowItem?.student?.personPicture?.personPictureThumbnailUri,
                                personName = rowItem?.student?.person?.fullName(),
                            )
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        uiState.courseBlocks.forEach { block ->
                            val result = rowItem?.blockStatuses?.firstOrNull {
                                it.sCbUid == block.block?.cbUid
                            }

                            val scoreScaled = result?.sScoreScaled
                            val maxPoints = block.block?.cbMaxPoints
                            val mark = if(scoreScaled != null && maxPoints != null) {
                                scoreScaled * maxPoints
                            }else {
                                null
                            }

                            Text(
                                text = mark?.roundTo(2)?.toString() ?: "-",
                                modifier = Modifier.width(COLUMN_WIDTH.dp),
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            FilledTonalIconButton(
                onClick = onClickZoomIn,
                enabled = uiState.canZoomIn,
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = null)
            }

            FilledTonalIconButton(
                onClick = onClickZoomOut,
                enabled = uiState.canZoomOut,
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = null)
            }

            IconButton(
                onClick = onClickFullScreen,
            ) {
                Icon(Icons.Default.Fullscreen, contentDescription = null)
            }
        }


    }

}