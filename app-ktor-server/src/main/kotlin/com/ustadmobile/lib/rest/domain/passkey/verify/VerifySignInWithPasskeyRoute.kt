package com.ustadmobile.lib.rest.domain.passkey.verify

import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.domain.credentials.passkey.model.AuthenticationResponseJSON
import io.github.aakira.napier.Napier
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.VerifySignInWithPasskeyRoute(
    useCase: (ApplicationCall) -> VerifySignInWithPasskeyUseCase,
) {
    post("verifypasskey") {
        val authenticationResponseJSON: AuthenticationResponseJSON = call.receive()
        val rpId = call.request.queryParameters["rpId"] ?: ""

        try {
            val response = useCase(call).invoke(
                authenticationResponseJSON = authenticationResponseJSON,
                rpId = rpId,
            )
            call.respond(response)

        } catch (e: UnauthorizedException) {
            call.respond(HttpStatusCode.Unauthorized)
        } catch (e: Throwable) {
            Napier.d { "verifypasskeyErr:-  ${e.message}" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}

