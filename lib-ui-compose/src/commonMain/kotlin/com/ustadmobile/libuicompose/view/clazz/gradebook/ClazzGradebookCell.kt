package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.clazz.gradebook.displayMarkFor
import com.ustadmobile.lib.db.composites.BlockStatus
import com.ustadmobile.libuicompose.components.scaledTextStyle
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.ext.maxScoreSummedIfModule
import com.ustadmobile.core.viewmodel.clazz.gradebook.aggregateIfModule
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.libuicompose.components.UstadTooltipBox

@Composable
fun ClazzGradebookCell(
    blockUid: Long,
    blockStatuses: List<BlockStatus>,
    blocks: List<CourseBlock>,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val block = blocks.firstOrNull { it.cbUid == blockUid }

    val blockStatus = blockStatuses.aggregateIfModule(blockUid, blocks)

    val markColors = blockStatus?.sScoreScaled?.let {
        block?.takeIf { blockStatus.sIsCompleted  }?.colorsForMark(it)
    }

    val maxPoints = block?.maxScoreSummedIfModule(allBlocks = blocks)

    Box(modifier) {
        val displayMark = blockStatus?.displayMarkFor(maxPoints)
        val progress = blockStatus?.sProgress

        when {
            /**
             * When mark is available - show it with colored background
             */
            displayMark != null -> {
                val textStyle = scaledTextStyle(scale)
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp * scale)
                        .let {
                            if(markColors != null) it.background(color = markColors.second) else it
                        }
                )

                Text(
                    text = displayMark,
                    modifier = Modifier.align(Alignment.Center),
                    style = textStyle.copy(
                        color = markColors?.first ?: textStyle.color
                    )
                )
            }

            /**
             * When there is no mark, however it is completed (e.g. video watched etc) - show
             * checkmark
             */
            blockStatus?.sIsCompleted == true || blockStatus?.sIsSuccess == true -> {
                UstadTooltipBox(
                    tooltipText = stringResource(MR.strings.completed),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(MR.strings.completed),
                    )
                }
            }

            /**
             * Edge case - there is no mark, however it is marked as a failure.
             */
            blockStatus?.sIsSuccess == false -> {
                UstadTooltipBox(
                    tooltipText = stringResource(MR.strings.failed),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(MR.strings.failed),
                    )
                }
            }

            progress != null -> {
                CircularProgressIndicator(
                    progress = { progress.toFloat() / 100f },
                    strokeWidth = 4.dp * scale,
                    modifier = Modifier.align(Alignment.Center)
                        .fillMaxSize()
                        .padding(16.dp * scale)
                )
            }

            else -> {
                //No info
                Text(
                    text = "-",
                    modifier = Modifier.align(Alignment.Center),
                    style = scaledTextStyle(scale)
                )
            }
        }
    }
}