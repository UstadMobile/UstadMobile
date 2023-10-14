package com.ustadmobile.libcache.headers

import com.ustadmobile.libcache.paramTokens

data class CouponHeader(
    val expectSha256: String? = null,
    val actualSha256: String? = null,
) {

    fun toHeaderString(): String {
        return buildString {
            if(expectSha256 != null)
                append("$EXPECT_SHA256_PARAM_NAME=$expectSha256;")
            if(actualSha256 != null)
                append("$ACTUAL_SHA256_PARAM_NAME=$actualSha256;")
        }
    }

    companion object {

        const val COUPON_HEADER_NAME = "Coupon"

        const val EXPECT_SHA256_PARAM_NAME = "expect-sha-256"

        const val ACTUAL_SHA256_PARAM_NAME = "actual-sha-256"

        fun fromString(headerValue: String): CouponHeader {
            val params = headerValue.paramTokens().toMap()
            return CouponHeader(
                expectSha256 = params[EXPECT_SHA256_PARAM_NAME],
                actualSha256 = params[ACTUAL_SHA256_PARAM_NAME],
            )
        }


    }

}