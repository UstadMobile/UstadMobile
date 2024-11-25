package com.ustadmobile.lib.rest.api.contentupload

import com.ustadmobile.ihttp.ktorserver.toIHttpRequest
import com.ustadmobile.lib.rest.domain.contententry.getsubtitletrackfromuri.GetSubtitleTrackFromUriServerUseCase
import com.ustadmobile.lib.rest.ext.tryOrRespondHttpApiException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.GetSubtitleTrackServerRoute(
    getSubtitleTrackServerUseCase: (ApplicationCall) -> GetSubtitleTrackFromUriServerUseCase,
) {
    post("getsubtitletrack") {
        tryOrRespondHttpApiException {
            val track = getSubtitleTrackServerUseCase(call).invoke(call.request.toIHttpRequest())

            call.respond(message = track,)
        }
    }
}
