package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun UstadProgressBarWithLabel(
    labelContent: @Composable () -> Unit,
    progress: () -> Float,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
    ) {
        labelContent()

        Text("${(progress() * 100).toInt()}%")
    }

    LinearProgressIndicator(
        progress = progress ,
        modifier = Modifier.fillMaxWidth().testTag("progress_bar"),
    )
}