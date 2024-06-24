package com.ustadmobile.libuicompose.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

@Composable
fun scaledTextStyle(scale: Float): TextStyle {
    val currentTextStyle = LocalTextStyle.current
    return if(scale != 1.0f) {
        currentTextStyle.copy(fontSize = currentTextStyle.fontSize * scale)
    }else {
        currentTextStyle
    }
}
