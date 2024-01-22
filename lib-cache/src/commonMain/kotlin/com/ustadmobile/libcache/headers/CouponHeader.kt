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


    }

}