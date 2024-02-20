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
         * Header that can be added to provide a partial response file. If the header is present,
         * the body will be stored in the given path.
         *
         * An additional file will be created to store metadata (with .json appended).
         *
         * If the given path exists, and the metadata exists, then the interceptor will try to use
         * a partial request to resume the download. It will send a Range request with the If-Range
         * header set to the etag.
         *
         * If the server responds with 206, then the response from the server will be appended to
         * the given path.
         */
        const val HEADER_X_INTERCEPTOR_PARTIAL_FILE = "X-Interceptor-Partial-File"

    }

}