package com.ustadmobile.lib.rest.domain.invite

import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.domain.invite.ContactUploadRequest
import com.ustadmobile.lib.rest.NotificationSender
import com.ustadmobile.lib.rest.domain.invite.ProcessInviteUseCase.InviteResult
import io.github.aakira.napier.Napier
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.json.Json
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

//Handle incoming requests from existing user to invite people
fun Route.ProcessInviteRoute(
    useCase: (ApplicationCall) -> ProcessInviteUseCase,

) {
    post("sendcontacts") {

        val request: ContactUploadRequest = call.receive()
        try {
            useCase.invoke(call)

            val response = useCase(call).invoke(
                contacts = request.contacts,
                personUid = request.personUid,
                role = request.role,
                clazzUid = request.clazzUid
            )
            Napier.d { "ProcessInvite response:-  ${response}" }

            call.respond(response)
        } catch (e: UnauthorizedException) {
            call.respond(HttpStatusCode.Unauthorized)
        } catch (e: Throwable) {
            Napier.d { "ProcessInvite Ex:-  ${e.message}" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
