package com.ustadmobile.lib.rest.domain.invite

import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.domain.invite.ResendInviteRequest
import io.github.aakira.napier.Napier
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

//Handle incoming requests from existing user to resend invite
fun Route.ResendInviteRoute(
    useCase: (ApplicationCall) -> ResendInviteUseCase,

) {
    post("sendcontact") {

        val request: ResendInviteRequest = call.receive()
        try {
            useCase.invoke(call)

            val response = useCase(call).invoke(
                contact = request.contacts,
                personUid = request.personUid,
            )
            Napier.d { "ResendInvite response:-  ${response}" }

            call.respond(response)
        } catch (e: UnauthorizedException) {
            call.respond(HttpStatusCode.Unauthorized)
        } catch (e: Throwable) {
            Napier.d { "ResendInvite Ex:-  ${e.message}" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
