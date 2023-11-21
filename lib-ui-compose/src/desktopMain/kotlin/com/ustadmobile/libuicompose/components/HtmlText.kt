package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText

@Composable
actual fun HtmlText(
    html: String,
    modifier: Modifier,
    htmlMaxLines: Int,
) {

    val richTextState = rememberRichTextState()

    richTextState.setHtml(html)

    RichText(
        state = richTextState,
        maxLines = htmlMaxLines,
        modifier = modifier
            .fillMaxWidth()
    )
}