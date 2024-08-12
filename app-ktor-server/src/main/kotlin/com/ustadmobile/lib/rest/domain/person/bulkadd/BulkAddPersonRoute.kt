package com.ustadmobile.lib.rest.domain.person.bulkadd

import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonStatusMap
import com.ustadmobile.core.domain.person.bulkadd.EnqueueBulkAddPersonServerUseCase
import com.ustadmobile.core.viewmodel.person.bulkaddrunimport.BulkAddPersonRunImportUiState
import com.ustadmobile.door.ext.requireRemoteNodeIdAndAuth
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.json.Json

//Handle incoming requests from web client for bulk user import
fun Route.BulkAddPersonRoute(
    enqueueBulkAddPersonServerUseCase: (ApplicationCall) -> EnqueueBulkAddPersonServerUseCase,
    bulkAddPersonStatusMap: (ApplicationCall) -> BulkAddPersonStatusMap,
    json: Json,
) {

    post("enqueue") {
        val (fromNode, auth) = requireRemoteNodeIdAndAuth()
        val accountPersonUid = call.request.queryParameters["accountPersonUid"]?.toLong() ?: 0
        val csvData = call.receiveText()
        val jobTimestamp = enqueueBulkAddPersonServerUseCase(call)
            .invoke(
                accountPersonUid = accountPersonUid,
                fromNodeId = fromNode,
                nodeAuth = auth,
                csvData = csvData
            )

        call.respondText(jobTimestamp.toString())
    }

    get("status") {
        val statusMap = bulkAddPersonStatusMap(call)
        val timestamp = call.request.queryParameters["timestamp"]?.toLong() ?: 0L
        val status = statusMap[timestamp]

        if(status != null) {
            call.respondText(
                contentType = ContentType.Text.Plain,
                text = json.encodeToString(
                    serializer = BulkAddPersonRunImportUiState.serializer(),
                    value = status,
                )
            )
        }else {
            call.respond(HttpStatusCode.NoContent, "")
        }
    }
}