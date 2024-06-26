package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.lib.db.composites.BlockStatus
import com.ustadmobile.lib.db.entities.CourseBlock

@Preview
@Composable
fun ClazzGradebookCellMarkedPreview() {
    ClazzGradebookCell(
        blockStatus = BlockStatus(
            sIsCompleted = true,
            sProgress = 100,
            sScoreScaled = 0.8f
        ),
        block = CourseBlock(
            cbMaxPoints = 10f,
        ),
        scale = 1f,
        modifier = Modifier.size(40.dp)
    )
}

@Preview
@Composable
fun ClazzGradebookCellCompletedPreview() {
    ClazzGradebookCell(
        blockStatus = BlockStatus(
            sIsCompleted = true,
            sProgress = 100,
        ),
        block = CourseBlock(
            cbMaxPoints = null,
        ),
        scale = 1f,
        modifier = Modifier.size(40.dp)
    )
}

@Preview
@Composable
fun ClazzGradebookCellProgressedPreview() {
    ClazzGradebookCell(
        blockStatus = BlockStatus(
            sProgress = 60,
        ),
        block = CourseBlock(
            cbMaxPoints = null,
        ),
        scale = 1f,
        modifier = Modifier.size(40.dp)
    )
}

@Preview
@Composable
fun ClazzGradebookCellEmptyPreview() {
    ClazzGradebookCell(
        blockStatus = BlockStatus(

        ),
        block = CourseBlock(
            cbMaxPoints = null,
        ),
        scale = 1f,
        modifier = Modifier.size(40.dp)
    )
}

