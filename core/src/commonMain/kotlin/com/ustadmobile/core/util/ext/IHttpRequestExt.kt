package com.ustadmobile.core.util.ext

import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.request.IHttpRequestWithByteBody
import com.ustadmobile.ihttp.request.IHttpRequestWithTextBody

suspend fun IHttpRequest.requireBodyAsText(): String {
    return (this as? IHttpRequestWithTextBody)?.bodyAsText() ?: throw HttpApiException(400, "No body")
}

suspend fun IHttpRequest.requireBodyAsBytes(): ByteArray {
    return (this as? IHttpRequestWithByteBody)?.bodyAsBytes() ?: throw HttpApiException(400, "No body")
}
