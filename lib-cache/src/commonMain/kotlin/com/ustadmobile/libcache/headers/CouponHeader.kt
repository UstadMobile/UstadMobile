package com.ustadmobile.libcache.headers


data class CouponHeader(
    val expectSha256: String? = null,
    val actualSha256: String? = null,
) {


    companion object {


        /**
         * Header that we use to assert that the Etag is a valid subresource integrity string
         */
        const val HEADER_ETAG_IS_INTEGRITY = "X-Etag-Is-Integrity"

        const val HEADER_X_INTEGRITY = "X-Integrity"

        /**
         * Special header that is used by lib-cache to allow other components to get the path
         * of the file where the cached data is stored. This is is useful for items that are accessed
         * by URL where the viewer needs a file (e.g. PDFs), and avoids the need to make (another)
         * temporary copy.
         *
         * If X-Request-Storage-Path is on the request, then a response delivered from the cache
         * will include X-Storage-Path. The first response (that is not cached) won't have the header.
         * The component that needs the storage path can simply "follow-up" using a HEAD request.
         */
        const val HEADER_X_REQUEST_STORAGE_PATH = "X-Request-Storage-Path"

        const val HEADER_X_RESPONSE_STORAGE_PATH = "X-Storage-Path"


    }

}