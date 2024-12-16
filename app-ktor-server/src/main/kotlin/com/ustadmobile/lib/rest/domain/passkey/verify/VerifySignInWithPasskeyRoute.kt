package com.ustadmobile.lib.rest.domain.passkey.verify

import com.ustadmobile.core.account.UnauthorizedException
import io.github.aakira.napier.Napier
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.VerifySignInWithPasskeyRoute(
    useCase: (ApplicationCall) -> VerifySignInWithPasskeyUseCase,
) {
    post("verifypasskey") {

        val credentialId = call.request.queryParameters["id"] ?: ""
        val userHandle = call.request.queryParameters["userHandle"] ?: ""
        val authenticatorData = call.request.queryParameters["authenticatorData"] ?: ""
        val clientDataJSON = call.request.queryParameters["clientDataJSON"] ?: ""
        val signature = call.request.queryParameters["signature"] ?: ""
        val origin = call.request.queryParameters["origin"] ?: ""
        val rpId = call.request.queryParameters["rpId"] ?: ""
        val challenge = call.request.queryParameters["challenge"] ?: ""

        try {
            val response = useCase(call).invoke(
                credentialId = credentialId,
                userHandle = userHandle,
                authenticatorData = authenticatorData,
                clientDataJSON = clientDataJSON,
                signature = signature,
                origin = origin,
                rpId = rpId,
                challenge = challenge,
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

