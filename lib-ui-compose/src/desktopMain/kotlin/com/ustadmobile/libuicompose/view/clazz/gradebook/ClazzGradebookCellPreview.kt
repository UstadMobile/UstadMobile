package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.lib.db.composites.BlockStatus
import com.ustadmobile.lib.db.entities.CourseBlock

private val PREVIEW_BLOCKUID = 42L

@Preview
@Composable
fun ClazzGradebookCellMarkedPreview() {
    ClazzGradebookCell(
        blockUid = PREVIEW_BLOCKUID,
        blockStatuses = listOf(
            BlockStatus(
                sCbUid = PREVIEW_BLOCKUID,
                sIsCompleted = true,
                sProgress = 100,
                sScoreScaled = 0.8f
            )
        ),
        blocks = listOf(
            CourseBlock(
                cbUid = PREVIEW_BLOCKUID,
                cbMaxPoints = 10f,
            )
        ),
        scale = 1f,
        modifier = Modifier.size(40.dp)
    )
}

@Preview
@Composable
fun ClazzGradebookCellCompletedPreview() {
    ClazzGradebookCell(
        blockUid = PREVIEW_BLOCKUID,
        blockStatuses = listOf(
            BlockStatus(
                sCbUid = PREVIEW_BLOCKUID,
                sIsCompleted = true,
                sProgress = 100,
            )
        ),
        blocks = listOf(
            CourseBlock(
                cbUid = PREVIEW_BLOCKUID,
                cbMaxPoints = null,
            )
        ) ,
        scale = 1f,
        modifier = Modifier.size(40.dp)
    )
}

@Preview
@Composable
fun ClazzGradebookCellProgressedPreview() {
    ClazzGradebookCell(
        blockUid = PREVIEW_BLOCKUID,
        blockStatuses = listOf(
            BlockStatus(
                sCbUid = PREVIEW_BLOCKUID,
                sProgress = 60,
            )
        ),
        blocks = listOf(
            CourseBlock(
                cbUid = PREVIEW_BLOCKUID,
                cbMaxPoints = null,
            )
        ),
        scale = 1f,
        modifier = Modifier.size(40.dp)
    )
}

@Preview
@Composable
fun ClazzGradebookCellEmptyPreview() {
    ClazzGradebookCell(
        blockUid = PREVIEW_BLOCKUID,
        blockStatuses = listOf(
            BlockStatus(
                sCbUid = PREVIEW_BLOCKUID,
            )
        ),
        blocks = listOf(
            CourseBlock(
                cbUid = PREVIEW_BLOCKUID,
                cbMaxPoints = null,
            ),
        ),
        scale = 1f,
        modifier = Modifier.size(40.dp)
    )
}

