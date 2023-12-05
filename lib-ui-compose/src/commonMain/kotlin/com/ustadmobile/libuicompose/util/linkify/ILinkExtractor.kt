package com.ustadmobile.libuicompose.util.linkify

interface ILinkExtractor {

    fun extractSpans(text: CharSequence): Iterable<ISpan>

}

