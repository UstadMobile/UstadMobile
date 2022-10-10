package com.ustadmobile.port.android.ui.compose

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography

@Composable
fun TextHeader1(text: String, color: Color) {
    Text(text = text, style = Typography.h1, color = color)
}

@Composable
fun TextBody1(text: String, color: Color) {
    Text(text = text, style = Typography.body1, color = color)
}