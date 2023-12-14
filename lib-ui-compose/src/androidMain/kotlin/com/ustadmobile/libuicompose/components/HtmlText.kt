package com.ustadmobile.libuicompose.components

import android.text.TextUtils
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.TextViewCompat
import com.ustadmobile.core.util.text.htmlTextToSpanned
import com.ustadmobile.libuicompose.R

@Composable
actual fun HtmlText(
    html: String,
    modifier: Modifier,
    htmlMaxLines: Int,
) {

    fun TextView.setHtmlText() {
        if(getTag(R.id.tag_textfield_html) != html) {
            text = html.htmlTextToSpanned()
            setTag(R.id.tag_textfield_html, html)
        }
        if(maxLines != htmlMaxLines) {
            maxLines = htmlMaxLines
            ellipsize = TextUtils.TruncateAt.END
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                TextViewCompat.setTextAppearance(this,
                    com.google.android.material.R.style.TextAppearance_MaterialComponents_Body1)
            }
        },
        update = {
            it.setHtmlText()
        }
    )
}