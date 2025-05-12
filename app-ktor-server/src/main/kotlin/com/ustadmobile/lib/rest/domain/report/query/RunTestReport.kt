package com.ustadmobile.lib.rest.domain.report.query

import com.ustadmobile.core.domain.report.query.DEFAULT_DURATION_PER_STATEMENT
import com.ustadmobile.core.domain.report.query.DEFAULT_NUM_DAYS
import com.ustadmobile.core.domain.report.query.DEFAULT_NUM_STATEMENTS_PER_DAY
import com.ustadmobile.core.domain.report.query.GenerateTestXapiStatementsUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.RunTestReport(
    generateTestXapiStatementsUseCase: (ApplicationCall) -> GenerateTestXapiStatementsUseCase,
) {
    post {
        val params = call.receiveParameters()

        val contentTitle = params["contentTitle"]
        val personUid = params["personUid"]?.toLongOrNull()
        val numDays = params["numDays"]?.toIntOrNull() ?: DEFAULT_NUM_DAYS
        val numStatementsPerDay =
            params["numStatementsPerDay"]?.toIntOrNull() ?: DEFAULT_NUM_STATEMENTS_PER_DAY
        val durationPerStatement =
            params["durationPerStatement"]?.toLongOrNull() ?: DEFAULT_DURATION_PER_STATEMENT

        if (contentTitle == null || personUid == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing contentEntryUid or personUid")
            )
            return@post
        }

        try {
            generateTestXapiStatementsUseCase(call).invoke(
                contentTitle = contentTitle,
                personUid = personUid,
                numDays = numDays,
                numStatementsPerDay = numStatementsPerDay,
                durationPerStatement = durationPerStatement
            )
            call.respond(
                HttpStatusCode.Created,
                mapOf("message" to "Successfully generated test statements")
            )
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }
}