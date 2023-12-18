package com.ustadmobile.libuicompose.util.linkify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.nibor.autolink.LinkExtractor
import org.nibor.autolink.LinkType

@Composable
actual fun rememberLinkExtractor(): ILinkExtractor {
    return remember {
        LinkExtractorAdapter(
            LinkExtractor.builder()
                .linkTypes(setOf(LinkType.URL, LinkType.URL))
                .build()
        )
    }
}