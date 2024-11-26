package com.ustadmobile.core.domain.contententry

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.manifest.ContentManifestEntry
import com.ustadmobile.core.url.UrlKmp
import com.ustadmobile.core.util.ext.removeHashSuffix
import com.ustadmobile.core.util.ext.removeQueryStringSuffix
import net.thauvin.erik.urlencoder.UrlEncoderUtil

/**
 * Simple map wrapper for ContentManifest - builds a map so lookups will be faster.
 *
 * @param entryMap Map of the path of a given ContentManifestEntry to the entry itself. Matching
 *        uris which might be encoded won't work because there can be different valid ways e.g.
 *        it is valid (if unusual) to encode plain ASCII etc.
 */
data class ContentManifestMap(
    val manifest: ContentManifest,
    val entryMap: Map<String, ContentManifestEntry> = manifest.entries.associateBy {
        UrlEncoderUtil.decode(it.uri)
    }
) {

    operator fun get(pathInContentEntryVersion: String): ContentManifestEntry? {
        return entryMap[pathInContentEntryVersion]
            ?: entryMap[
                pathInContentEntryVersion.removeQueryStringSuffix().removeHashSuffix()
            ]?.takeIf { it.ignoreQueryParams }
    }

    fun resolveUrl(
        manifestUrl: String,
        pathInContentEntryVersion: String
    ): String {
        return UrlKmp(manifestUrl).resolve(pathInContentEntryVersion).toString()
    }
}
