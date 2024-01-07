package com.ustadmobile.lib.rest.api.content

import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerUseCase
import com.ustadmobile.lib.rest.ext.respondOkHttpResponse
import com.ustadmobile.lib.rest.util.toCacheHttpRequest
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.ContentEntryVersionRoute(
    useCase: (ApplicationCall) -> ContentEntryVersionServerUseCase
) {
    get("{contentEntryVersionUid}/{pathInContent...}") {
        val contentEntryVersionUid = call.parameters["contentEntryVersionUid"]?.toLong() ?: 0
        val pathInContent = call.parameters["pathInContent"] ?: ""
        val useCaseForCall = useCase(call)
        val response = useCaseForCall(
            request = call.request.toCacheHttpRequest(),
            contentEntryVersionUid = contentEntryVersionUid,
            pathInContentEntryVersion = pathInContent
        )
        call.respondOkHttpResponse(response)
    }
}
