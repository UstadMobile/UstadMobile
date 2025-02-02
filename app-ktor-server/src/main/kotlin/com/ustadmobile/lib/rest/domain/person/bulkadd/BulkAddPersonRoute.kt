package com.ustadmobile.lib.rest.domain.person.bulkadd

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonStatusMap
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsUseCase
import com.ustadmobile.core.domain.person.bulkadd.EnqueueBulkAddPersonServerUseCase
import com.ustadmobile.core.viewmodel.person.bulkaddrunimport.BulkAddPersonRunImportUiState
import com.ustadmobile.door.ext.requireRemoteNodeIdAndAuth
import com.ustadmobile.lib.rest.ext.requireBasicAuth
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
    bulkAddPersonUseCase: (ApplicationCall) -> BulkAddPersonsUseCase,
    bulkAddPersonStatusMap: (ApplicationCall) -> BulkAddPersonStatusMap,
    authManager: (ApplicationCall) -> AuthManager,
    db: (ApplicationCall) -> UmAppDatabase,
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

    //Run the bulk import directly using username/password auth (mostly for the benefit of end-to-end tests)
    post("import") {
        val basicAuth = call.request.requireBasicAuth()
        val authManagerVal = authManager(call)
        val authResult = authManagerVal.authenticate(basicAuth.username, basicAuth.password)
        val hasPermission = authResult.authenticatedPerson?.personUid?.let { personUid ->
            db(call).systemPermissionDao().personHasSystemPermission(
                accountPersonUid = personUid,
                permission = PermissionFlags.ADD_PERSON
            )
        } ?: false

        if(!authResult.success || !hasPermission) {
            call.respondText(status = HttpStatusCode.Unauthorized, text = "Invalid username/password or insufficient permission")
        }

        val csvData = call.receiveText()
        val result = bulkAddPersonUseCase(call).invoke(csv = csvData, onProgress = { _, _ -> })
        call.respondText("OK - imported ${result.numImported}")
    }

}