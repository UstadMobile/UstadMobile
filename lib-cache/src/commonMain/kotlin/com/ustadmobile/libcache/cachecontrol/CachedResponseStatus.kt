package com.ustadmobile.libcache.cachecontrol

/**
 * Represents the status of a cached response.
 *
 * @param isFresh if the response is fresh or not. This might be set to true, even if the response
 *        is technically stale, when it is allowed to use a stale response because the only-if-cached
 *        request directive was used or the max-stale directive was used to allow a stale response
 * @param ifNoneMatch if not fresh, and the original response had an etag, then this will provide the
 *        etag that can be used for if-none-match validation
 * @param ifNotModifiedSince if not fresh, and the original response had a last-modified header,
 *        this will provide the last-modified date that can be used for validation
 */
class CachedResponseStatus(
    val isFresh: Boolean,
    val ifNoneMatch: String?,
    val ifNotModifiedSince: String?,
) {

    /**
     * Whether or not it is possible to validate the response
     */
    val canBeValidated: Boolean
        get() = ifNoneMatch != null || ifNotModifiedSince != null

}
