package com.ustadmobile.libuicompose.components

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ustadmobile.core.util.text.htmlTextToSpanned
import com.ustadmobile.libuicompose.R

/**
 * Jetpack compose does not support Html.fromHtml spannable like views do. We need something that
 * looks like a textfield that can take users to a full text rich text editor when they tap it, and
 * will display the formatted html inside it.
 *
 * This uses an AndroidView wrapper, TextInputLayout, and puts a TextView that uses the HTML.fromHtml
 * Spanned string over the top in the correct position.
 */
@Composable
fun HtmlClickableTextField(
    html: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxHtmlLines: Int = 3,
) {

    val clickListener = remember(onClick) {
        View.OnClickListener {
            onClick()
        }
    }

    fun TextView.updateHtml() {
        val currentHtml = getTag(R.id.tag_textfield_html)
        if(currentHtml != html) {
            text = html.htmlTextToSpanned()
            setTag(R.id.tag_textfield_html, html)
        }
        
        if(maxLines != maxHtmlLines) {
            maxLines = maxHtmlLines
            ellipsize = TextUtils.TruncateAt.END
        }
    }

    fun TextInputLayout.updateHint() {
        if(hint != label)
            hint = label
    }

    fun TextInputEditText.updateText() {
        val inputText = if(html.isEmpty()) "" else " "
        if(text.toString() != inputText)
            setText(inputText)
    }

    fun View.update() {
        findViewById<TextInputLayout>(R.id.text_input_layout).updateHint()
        findViewById<TextInputEditText>(R.id.text_input_edit_text).apply {
            updateText()
            setOnClickListener(clickListener)
        }
        findViewById<TextView>(R.id.text_input_layout_textview).updateHtml()
    }



    AndroidView(
        modifier = modifier,
        factory = { context ->
            LayoutInflater.from(context).inflate(
                R.layout.item_text_input_layout, null, false
            ).apply {
                update()
                findViewById<TextInputEditText>(
                    R.id.text_input_edit_text
                ).setOnFocusChangeListener { v, hasFocus ->
                    if(hasFocus)
                        v.callOnClick()
                }
            }
        },
        update = {
            it.update()
        }
    )
}