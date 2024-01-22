package com.ustadmobile.lib.rest.domain.account

import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.domain.account.SetPasswordServerUseCase
import com.ustadmobile.door.ext.requireRemoteNodeIdAndAuth
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post


fun Route.SetPasswordRoute(
    useCase: (ApplicationCall) -> SetPasswordServerUseCase,
) {
    post("setpassword") {
        val (fromNode, auth) = requireRemoteNodeIdAndAuth()
        val activeUserUid = call.request.queryParameters["nodeActiveUserUid"]?.toLong() ?: 0
        val personUid = call.request.queryParameters["personUid"]?.toLong() ?: 0
        val username = call.request.queryParameters["username"] ?: ""
        val currentPassword = call.request.queryParameters["currentPassword"]
        val newPassword = call.request.queryParameters["newPassword"]
            ?: throw IllegalStateException("No newpassword")
        try {
            useCase(call).invoke(
                fromNodeId = fromNode,
                nodeAuth = auth,
                nodeActiveUserUid = activeUserUid,
                personUid = personUid,
                username = username,
                currentPassword = currentPassword,
                newPassword = newPassword,
            )
            call.respond(HttpStatusCode.NoContent)
        }catch(e: UnauthorizedException) {
            call.respond(HttpStatusCode.Unauthorized)
        }catch(e: Throwable) {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}