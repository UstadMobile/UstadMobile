package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.charlex.compose.material3.HtmlText

@Composable
actual fun UstadHtmlText(
    html: String,
    modifier: Modifier,
    htmlMaxLines: Int,
) {
    HtmlText(
        modifier = modifier,
        text = html,
    )
}
