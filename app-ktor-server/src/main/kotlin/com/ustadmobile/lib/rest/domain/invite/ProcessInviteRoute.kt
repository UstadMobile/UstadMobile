package com.ustadmobile.lib.rest.domain.invite

import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactChip
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.json.Json


fun Route.ProcessInviteRoute(
    useCase: (ApplicationCall) -> ProcessInviteUseCase,
    json: Json
) {
    post("sendcontacts") {

        val role = call.request.queryParameters["role"]?.toLong() ?: 0
        val personUid = call.request.queryParameters["personUid"]?.toLong() ?: 0
        val clazzUid = call.request.queryParameters["clazzUid"]?.toLong() ?: 0
        val contacts = call.request.queryParameters["contacts"]
            ?: throw IllegalStateException("No Contact")

        val contactList=json.decodeFromString<List<String>>(contacts)

        try {
            useCase(call).invoke(
                contacts = contactList,
                personUid = personUid,
                role = role,
                clazzUid = clazzUid
            )
            call.respond(HttpStatusCode.NoContent)
        } catch (e: UnauthorizedException) {
            call.respond(HttpStatusCode.Unauthorized)
        } catch (e: Throwable) {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

}
