package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import com.ustadmobile.libuicompose.util.linkify.ILinkExtractor
import com.ustadmobile.libuicompose.util.linkify.LinkISpan


const val TAG_LINK = "TAG_LINK"

/**
 * Uses LinkExtractor and ClickableText to extract links in text, then uses ClickableText to
 * handle onClick and uriHandle to handle link clicks
 */
@OptIn(ExperimentalTextApi::class, ExperimentalFoundationApi::class)
@Composable
fun UstadLinkifyText(
    text: String,
    linkExtractor: ILinkExtractor,
    linkStyle: SpanStyle = SpanStyle(
        color = Color.Blue,
        textDecoration = TextDecoration.Underline,
    ),
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val defaultSpanStyle = SpanStyle(
        color = MaterialTheme.colorScheme.onBackground
    )

    val links = remember(text){
        buildAnnotatedString {
            linkExtractor.extractSpans(text).forEach { span ->
                val spanText = text.substring(span.beginIndex, span.endIndex)
                if(span is LinkISpan) {
                    withAnnotation(TAG_LINK, spanText) {
                        withStyle(linkStyle) {
                            append(spanText)
                        }
                    }
                }else {
                    withStyle(defaultSpanStyle) {
                        append(spanText)
                    }
                }
            }
        }
    }

    var pointerIcon: PointerIcon by remember {
        mutableStateOf(PointerIcon.Default)
    }

    ClickableText(
        modifier = modifier.pointerHoverIcon(pointerIcon),
        text = links,
        onClick = { position ->
            links.getStringAnnotations(TAG_LINK, position, position).firstOrNull()?.also { annotation ->
                uriHandler.openUri(annotation.item)
            }
        },
        onHover = {
            val icon = if(it != null &&
                links.getStringAnnotations(TAG_LINK, it, it).isNotEmpty()
            ) {
                PointerIcon.Hand
            }else {
                PointerIcon.Default
            }

            if(pointerIcon != icon)
                pointerIcon = icon
        }
    )
}
