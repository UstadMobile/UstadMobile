package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun UstadTooltipBox(
    tooltipText: String,
    textColor: Color,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    TooltipArea(
        modifier = modifier,
        tooltip = {
            Surface(
                modifier = Modifier.shadow(4.dp),
                color = Color(255, 255, 210),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(tooltipText,
                    style = TextStyle(color = textColor),
                    modifier = Modifier.padding(10.dp))
            }
        },
        content = content,
    )
}