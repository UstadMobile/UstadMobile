package com.ustadmobile.core.domain.xapi.http

import com.ustadmobile.core.domain.interop.HttpApiException
import com.ustadmobile.core.domain.xapi.state.XapiStateParams
import com.ustadmobile.ihttp.request.IHttpRequest

fun IHttpRequest.queryParamOrThrow(paramName: String): String {
    return this.queryParam(paramName) ?: throw HttpApiException(400, "Missing $paramName")
}

fun IHttpRequest.xapiStateParams() = XapiStateParams(
    activityId = queryParamOrThrow("activityId"),
    agent = queryParamOrThrow("agent"),
    registration = queryParam("registration"),
    stateId = queryParamOrThrow("stateId")
)