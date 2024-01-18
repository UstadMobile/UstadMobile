package com.ustadmobile.core.util

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.manifest.ContentManifestEntry

fun ContentManifest.entryByUri(uri: String): ContentManifestEntry? {
    return entries.firstOrNull { it.uri == uri }
}

fun ContentManifest.requireEntryByUri(uri: String): ContentManifestEntry {
    return entryByUri(uri) ?: throw IllegalArgumentException("Manifest has no entry for $uri")
}

fun ContentManifest.bodyDataUrlForUri(uri: String) = entryByUri(uri)?.bodyDataUrl

fun ContentManifest.requireBodyUrlForUri(uri: String) = requireEntryByUri(uri).bodyDataUrl
