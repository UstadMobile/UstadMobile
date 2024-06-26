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
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.libuicompose.components.scaledTextStyle

@Composable
fun ClazzGradebookCell(
    blockStatus: BlockStatus?,
    block: CourseBlock?,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val markColors = blockStatus?.sScoreScaled?.let {
        block?.colorsForMark(it)
    }

    Box(
        modifier.let {
            if(markColors != null) it.background(color = markColors.second) else it
        }
    ) {
        val displayMark = blockStatus?.displayMarkFor(block)
        val progress = blockStatus?.sProgress

        when {
            /**
             * When mark is available - show it with colored background
             */
            displayMark != null -> {
                val textStyle = scaledTextStyle(scale)
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
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            /**
             * Edge case - there is no mark, however it is marked as a failure.
             */
            blockStatus?.sIsSuccess == false -> {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center)
                )
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