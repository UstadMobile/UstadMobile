package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun UstadHorizontalPagingIndicator(
    pageCount: Int,
    activePage: Int,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        (0 until pageCount).forEach { index ->
            Box(
                modifier = Modifier.size(8.dp).clip(CircleShape).background(
                    if(index == activePage) {
                        activeColor
                    }else {
                        inactiveColor
                    }
                )
            )
            if(index < pageCount - 1) {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}