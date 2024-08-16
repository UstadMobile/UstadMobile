package com.ustadmobile.lib.rest.domain.xapi

import com.ustadmobile.core.domain.xapi.http.XapiHttpServerUseCase
import com.ustadmobile.ihttp.ktorserver.respondIHttpResponse
import com.ustadmobile.ihttp.ktorserver.toIHttpRequest
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.routing.Route

fun Route.XapiRoute(
    xapiHttpServerUseCase: (ApplicationCall) -> XapiHttpServerUseCase
) {
    handle {
        val pathSegments = call.parameters.getAll("pathSegments") ?: emptyList()
        val iRequest = call.request.toIHttpRequest()
        val response = xapiHttpServerUseCase(call).invoke(pathSegments, iRequest)
        call.respondIHttpResponse(response, iRequest)
    }
}
