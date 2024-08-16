package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.libuicompose.util.ext.scaledDefaultItemPadding

@Composable
fun ScaledListItem(
    headlineContent: @Composable () -> Unit,
    leadingContent: @Composable () -> Unit,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.scaledDefaultItemPadding(scale = scale),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp * scale)
        ) {
            leadingContent()
        }

        Spacer(Modifier.width(8.dp * scale))

        Column(Modifier.weight(1.0f)) {
            headlineContent()
        }
    }
}