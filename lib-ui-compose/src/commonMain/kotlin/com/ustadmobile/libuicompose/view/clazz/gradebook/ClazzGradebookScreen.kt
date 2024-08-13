package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.clazz.gradebook.ClazzGradebookUiState
import com.ustadmobile.core.viewmodel.clazz.gradebook.ClazzGradebookViewModel
import com.ustadmobile.libuicompose.components.ScaledListItem
import com.ustadmobile.libuicompose.components.UstadNothingHereYet
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.scaledTextStyle
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

private const val NAME_WIDTH = 240

private const val COLUMN_WIDTH = 72

private const val HEADER_HEIGHT = 192

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

    val animatedScale by animateFloatAsState(
        targetValue = uiState.scale
    )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val nameColWidth = minOf(maxWidth / 2, (NAME_WIDTH * animatedScale).dp)
        val headerHeight = minOf(maxHeight / 2, HEADER_HEIGHT.dp)
        val scaledHeaderHeight = headerHeight * animatedScale
        val scaledColumnWidth = (COLUMN_WIDTH * animatedScale).dp
        val scaledRowHeight = scaledColumnWidth

        ClazzGradebookLazyColumn(
            horizontalScrollState = horizontalScrollState,
            lazyListState = rememberLazyListState(),
            stickyHeight = headerHeight,
            stickyWidth = nameColWidth,
            scale = animatedScale,
            modifier = Modifier.fillMaxSize()
        ) {
            if(!listResult.isSettledEmpty) {
                stickyHeader {
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxWidth()
                            .height(scaledHeaderHeight)
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
                                    width = scaledColumnWidth,
                                    height = scaledHeaderHeight,
                                    scale = animatedScale
                                )
                            }
                        }
                    }
                }
            }

            if(listResult.isSettledEmpty) {
                item("empty_state") {
                    UstadNothingHereYet()
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
                    ScaledListItem(
                        modifier = Modifier.width(nameColWidth).height(scaledRowHeight),
                        headlineContent = {
                            Text(
                                text = rowItem?.student?.person?.fullName() ?: "-",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = scaledTextStyle(animatedScale)
                            )
                        },
                        leadingContent = {
                            UstadPersonAvatar(
                                pictureUri = rowItem?.student?.personPicture?.personPictureThumbnailUri,
                                personName = rowItem?.student?.person?.fullName(),
                                modifier = Modifier.size((40 * animatedScale).dp),
                                fontScale = animatedScale,
                            )
                        },
                        scale = animatedScale
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

                            ClazzGradebookCell(
                                blockStatus = result,
                                block = block.block,
                                scale = animatedScale,
                                modifier = Modifier.width(scaledColumnWidth)
                                    .height(scaledRowHeight)
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
                enabled = uiState.canIncreaseScale,
            ) {
                Icon(Icons.Default.TextIncrease, contentDescription = null)
            }

            FilledTonalIconButton(
                onClick = onClickZoomOut,
                enabled = uiState.canDecreaseScale,
            ) {
                Icon(Icons.Default.TextDecrease, contentDescription = null)
            }

            IconButton(
                onClick = onClickFullScreen,
            ) {
                if(uiState.isFullScreen) {
                    Icon(Icons.Default.FullscreenExit, contentDescription = null)
                }else {
                    Icon(Icons.Default.Fullscreen, contentDescription = null)
                }

            }
        }


    }

}