package com.ustadmobile.lib.rest.domain.report.query

import com.ustadmobile.core.domain.report.query.RunReportUseCase
import com.ustadmobile.door.ext.requireRemoteNodeIdAndAuth
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.json.Json

fun Route.RunReportRoute(
    runReportServerUseCase: (ApplicationCall) -> RunReportServerUseCase,
    json: Json,
) {
    post("run") {
        val (fromNode, auth) = requireRemoteNodeIdAndAuth()
        val requestTxt = call.receiveText()

        val request = json.decodeFromString(
            RunReportUseCase.RunReportRequest.serializer(), requestTxt
        )

        val result = runReportServerUseCase(call).invoke(
            request = request, fromNodeId = fromNode, nodeAuth = auth
        )

        call.respondText(
            contentType = ContentType.Application.Json,
            text = json.encodeToString(
                RunReportUseCase.RunReportResult.serializer(), result
            )
        )
    }
}
