package com.ustadmobile.core.contentformats.manifest

import com.ustadmobile.core.util.stringvalues.IStringValues
import com.ustadmobile.core.util.stringvalues.emptyStringValues
import kotlinx.serialization.Serializable

/**
 * @param uri The URI as it will be used within the ContentEntry - this can be relative or absolute.
 *            e.g. images/picture.png or https://server.com/image/picture.png
 * @param storageSize the size as it will be transferred and stored. If the content is gzipped,
 *        then this should be the size after gzip compression. This is ONLY to give an indication of
 *        the likely size as inflating and compressing again might lead to minor variances in size.
 * @param ignoreQueryParams used to bust cache-busting. Required for H5P files where scripts add
 *        query parameters (eg. date) to static files.
 * @param status http status code e.g. 200
 * @param method http method e.g. GET
 * @param integrity the subresource integrity string e.g. sha256-... as per
 *        https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity . Currently
 *        ONLY SHA256 is supported.
 * @param requestHeaders (optional)
 * @param responseHeaders list of response headers as they will be served to the client when content
 *        is being served.
 * @param bodyDataUrl the source from which the body can actually be downloaded, normally the blob url
 *        which is normalized by the SHA256 checksum of the data.
 */
@Serializable
class ContentManifestEntry(
    val uri: String,
    val storageSize: Long,
    val ignoreQueryParams: Boolean = true,
    val status: Int = 200,
    val method: String = "GET",
    val integrity: String,
    val requestHeaders: IStringValues = emptyStringValues(),
    val responseHeaders: IStringValues,
    val bodyDataUrl: String,
)
