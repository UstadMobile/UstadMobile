package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ustadmobile.lib.db.composites.BlockStatus
import com.ustadmobile.libuicompose.theme.onSuccessContainerDark
import com.ustadmobile.libuicompose.theme.successContainerDark

/**
 * Given the BlockStatus, show a progress bar if determinative progress is available.
 * Show icons if there is a definitive pass/fail status.
 */
@Composable
fun UstadBlockStatusProgressBar(
    blockStatus: BlockStatus?,
    modifier: Modifier = Modifier,
    iconSize: Float = 16f,
    iconOutlineSize: Float = 2f,
) {
    val hasIcon = blockStatus?.sIsCompleted == true || blockStatus?.sIsSuccess != null

    Box(modifier) {
        blockStatus?.sProgress?.also { progress ->
            LinearProgressIndicator(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .let { if(hasIcon) it.padding(start = (8.dp)) else it },
                progress = { (progress.toFloat() / 100) },
            )
        }

        if(blockStatus?.sIsCompleted == true && blockStatus.sIsSuccess == false) {
            PaddedCircleIcon(
                imageVector = Icons.Default.Close,
                modifier = Modifier.align(Alignment.CenterStart),
                containerColor = MaterialTheme.colorScheme.error,
                onContainerColor = MaterialTheme.colorScheme.onError,
                size = iconSize,
                outlineSize = iconOutlineSize,
            )
        }else if(blockStatus?.sIsCompleted == true) {
            PaddedCircleIcon(
                imageVector = Icons.Default.Check,
                modifier = Modifier.align(Alignment.CenterStart),
                containerColor = successContainerDark,
                onContainerColor = onSuccessContainerDark,
                size = iconSize,
                outlineSize = iconOutlineSize,
            )
        }

    }
}

@Composable
private fun PaddedCircleIcon(
    size: Float = 16f,
    outlineSize: Float = 2f,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color,
    onContainerColor: Color,
    contentDescription: String? = null,
) {
    Box(
        modifier.size(size.dp)
            .background(color = MaterialTheme.colorScheme.background, shape = CircleShape)
    ) {
        Box(
            modifier
                .size((size - outlineSize).dp)
                .background(color = containerColor, shape = CircleShape)
                .align(Alignment.Center),
        ) {
            Icon(
                imageVector = imageVector,
                modifier = Modifier
                    .size((size - outlineSize).dp)
                    .align(Alignment.Center),
                tint = onContainerColor,
                contentDescription = contentDescription,
            )
        }
    }
}

