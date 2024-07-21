package com.ustadmobile.ihttp.request

import com.ustadmobile.ihttp.headers.IHttpHeaders


open class BaseHttpRequest(
    override val url: String,
    override val headers: IHttpHeaders,
    override val method: IHttpRequest.Companion.Method,
): IHttpRequest
