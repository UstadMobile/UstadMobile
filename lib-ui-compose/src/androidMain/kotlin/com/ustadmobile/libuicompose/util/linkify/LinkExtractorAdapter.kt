package com.ustadmobile.libuicompose.util.linkify

import org.nibor.autolink.LinkExtractor
import org.nibor.autolink.LinkSpan

class LinkExtractorAdapter(
    private val extractor: LinkExtractor
): ILinkExtractor {

    override fun extractSpans(text: CharSequence): Iterable<ISpan> {
        return extractor.extractSpans(text).map {
            if(it is LinkSpan) {
                LinkISpan(it.beginIndex, it.endIndex)
            }else {
                TextISpan(it.beginIndex, it.endIndex)
            }
        }
    }

}