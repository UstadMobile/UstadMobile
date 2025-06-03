package com.ustadmobile.lib.rest.domain.username

import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.username.UsernameSuggestionUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.UsernameSuggestionRoute(
    usernameSuggestionUseCase: (ApplicationCall) ->  UsernameSuggestionUseCase
) {
    post("getsuggestion") {
        val name = call.request.queryParameters["name"]
            ?: throw IllegalStateException("No username found")
        try {
            val response = usernameSuggestionUseCase(call).invoke(
                name = name
            )
            call.respond(response)
        } catch (e: UnauthorizedException) {
            call.respond(HttpStatusCode.Unauthorized)
        } catch (e: Throwable) {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}