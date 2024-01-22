package com.ustadmobile.core.contentformats.manifest

import kotlinx.serialization.Serializable

/**
 * The ContentManifest is used to facilitate serving content (e.g. ebooks, video, xapi, HTML5, etc)
 * over the web or offline. It contains a list of entries (which can be relative or absolute uris)
 * and the http request and response info required to serve it. It is conceptually similar to a HAR
 * file, but intended to be simpler and does not include timing information etc.
 *
 * Each entry contains:
 *  A URI within the content: this can be relative such as images/logo.png or absolute .
 *  The HTTP response headers that will be served
 *  The subresource integrity (SHA 256 checksum) of the entry
 *  A cacheable URL (bodyDataUrl) from where the body can be downloaded. This URL is typically a blob URL
 *  which is normalized by SHA-256. This ensures that where multiple content items share assets (eg.
 *  common javascripts, CSS, etc) only one copy of the asset need be downloaded and stored.
 *
 * This manifest provides all the information required by a client to know which files would be
 * required for offline use so that they can be downloaded and stored in the cache if requested by
 * the user.
 */
@Serializable
data class ContentManifest(
    val version: Int,
    val metadata: Map<String, String>,
    val entries: List<ContentManifestEntry>,
)
