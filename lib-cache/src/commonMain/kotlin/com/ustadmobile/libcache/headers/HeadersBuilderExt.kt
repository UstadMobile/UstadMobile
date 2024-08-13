package com.ustadmobile.libcache.headers

import com.ustadmobile.ihttp.headers.IHeadersBuilder
import com.ustadmobile.ihttp.headers.IHttpHeaders

/**
 * Add the integrity:  if the etag header is not already taken, then use the etag field. Otherwise
 * use x-integrity.
 */
internal fun IHeadersBuilder.addIntegrity(
    extraHeaders: IHttpHeaders?,
    integrity: String
) {
    if(extraHeaders?.containsHeader("etag") != true) {
        header("etag", integrity)
        header(CouponHeader.HEADER_ETAG_IS_INTEGRITY, "true")
    }else if(!extraHeaders.containsHeader(CouponHeader.HEADER_X_INTEGRITY)) {
        header(CouponHeader.HEADER_X_INTEGRITY, integrity)
    }
}