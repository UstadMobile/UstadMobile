package com.ustadmobile.libcache.headers

import com.ustadmobile.ihttp.headers.IHttpHeader

expect fun lastModifiedHeader(
    time: Long = 0
): IHttpHeader
