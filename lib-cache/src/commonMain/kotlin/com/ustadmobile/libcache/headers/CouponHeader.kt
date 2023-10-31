package com.ustadmobile.libcache.headers


data class CouponHeader(
    val expectSha256: String? = null,
    val actualSha256: String? = null,
) {


    @Suppress("unused")
    companion object {

        const val COUPON_EXPECT_SHA_256_HEADER_NAME = "Coupon-Expect-Sha-256"

        const val COUPON_EXPECT_CONTENT_TYPE_HEADER_NAME = "Coupon-Expect-Content-Type"

        const val COUPON_EXPECT_SIZE = "Coupon-Expect-Size"

        const val COUPON_ACTUAL_SHA_256 = "Coupon-Actual-Sha-256"

        const val COUPON_HEADER_NAME = "Coupon"

        const val COUPON_STATIC = "Coupon-Static"


    }

}