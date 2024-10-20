package com.ustadmobile.lib.rest.ext

import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.ihttp.request.IHttpRequest

fun IHttpRequest.requireQueryParamOrThrow(
    name: String,
    errorCode: Int = 400,
    errorMessage: String = "Missing required query parameter: $name"
): String {
    return queryParam(name) ?: throw HttpApiException(errorCode, errorMessage)
}
