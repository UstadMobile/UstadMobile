package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.composites.BlockStatus


@Composable
@Preview
fun UstadBlockStatusProgressBarPreview() {
    UstadBlockStatusProgressBar(
        blockStatus = BlockStatus(
            sIsCompleted = true,
            sProgress = 100,
        )
    )
}

@Composable
@Preview
fun UstadBlockStatusProgressBarFailPreview() {
    UstadBlockStatusProgressBar(
        blockStatus = BlockStatus(
            sIsCompleted = true,
            sIsSuccess = false,
            sProgress = 50,
        )
    )
}