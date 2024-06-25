package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ustadmobile.core.util.ext.toDisplayString
import com.ustadmobile.lib.db.composites.BlockStatus
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.libuicompose.components.UstadBlockStatusProgressBar
import com.ustadmobile.libuicompose.components.scaledTextStyle

@Composable
fun ClazzGradebookCell(
    blockStatus: BlockStatus?,
    block: CourseBlock?,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        val maxPoints = block?.cbMaxPoints
        val scoreScaled = blockStatus?.sScoreScaled
        val points = if(maxPoints != null && scoreScaled != null) {
            maxPoints * scoreScaled
        }else {
            null
        }

        Text(
            text = points?.toDisplayString() ?: "-",
            modifier = Modifier.align(Alignment.Center),
            style = scaledTextStyle(scale)
        )

        UstadBlockStatusProgressBar(
            blockStatus = blockStatus,
            modifier = Modifier.align(Alignment.BottomCenter),
            iconSize = 16f * scale,
            iconOutlineSize = 2f * scale,
        )
    }
}