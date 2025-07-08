package com.ustadmobile.lib.rest.domain.account

import com.ustadmobile.core.account.SendConsentRequestToParentUseCase
import com.ustadmobile.core.account.UnauthorizedException
import io.github.aakira.napier.Napier
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.SendConsentRequestToParentRoute(
    useCase: (ApplicationCall) -> SendConsentRequestToParentUseCase,
) {
    post("sendcensentrequest") {
        val request: SendConsentRequestToParentUseCase.SendConsentRequestToParentRequest = call.receive()

        try {
            useCase(call).invoke(request)
            call.respond(status = HttpStatusCode.OK, message = "")
        } catch (e: UnauthorizedException) {
            call.respond(HttpStatusCode.Unauthorized)
        } catch (e: Throwable) {
            Napier.d { "ProcessInvite Ex:-  ${e.message}" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
