package com.ustadmobile.lib.rest.domain.report.query

import com.ustadmobile.core.domain.report.query.GenerateTestXapiStatementsUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.RunTestReport(
    generateTestXapiStatementsUseCase: (ApplicationCall) -> GenerateTestXapiStatementsUseCase,
) {
    get("runtest") {
        val contentTitle = call.request.queryParameters["contentTitle"] ?: ""
        val username = call.request.queryParameters["username"] ?: ""
        if (username.isEmpty()) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "username parameter is required")
            )
            return@get
        }
        try {
            generateTestXapiStatementsUseCase(call).invoke(
                contentTitle = contentTitle,
                personName = username,
            )
            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Successfully generated test statements")
            )
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }
}