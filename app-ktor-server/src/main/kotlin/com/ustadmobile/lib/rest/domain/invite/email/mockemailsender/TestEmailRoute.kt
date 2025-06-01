package com.ustadmobile.lib.rest.domain.invite.email.mockemailsender

import io.ktor.server.routing.Route
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
fun Route.TestEmailRoute (
    mockEmailSender: MockEmailSender
){
    get("list") {
        val to = call.request.queryParameters["to"]
        if (to.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Missing 'to' parameter")
            return@get
        }

        val emails = mockEmailSender.getEmailsForUser(to)
        call.respond(
            if (emails.isNotEmpty()) HttpStatusCode.OK else HttpStatusCode.NotFound,
            emails
        )
    }
}