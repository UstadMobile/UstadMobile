package com.ustadmobile.lib.rest.ext

import com.ustadmobile.core.domain.interop.HttpApiException
import io.ktor.http.Parameters

fun Parameters.requireParamOrThrow(name: String): String {
    return get(name) ?: throw HttpApiException(400, "Missing parameter $name")
}
