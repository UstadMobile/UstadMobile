package com.ustadmobile.lib.rest.domain.contententry.importcontent

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.contententry.importcontent.CancelImportContentEntryServerUseCase
import com.ustadmobile.door.ext.requireRemoteNodeIdAndAuth
import com.ustadmobile.lib.db.composites.ContentEntryImportJobProgress
import io.github.aakira.napier.Napier
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

fun Route.ContentEntryImportJobRoute(
    json: Json,
    dbFn: (ApplicationCall) -> UmAppDatabase,
    cancelImportContentEntryServerUseCase: (ApplicationCall) -> CancelImportContentEntryServerUseCase,
) {
    get("importjobs") {
        val contentEntryUid = call.request.queryParameters["contententryuid"]?.toLong() ?: 0
        val db = dbFn(call)
        val inProgressJobs = db.contentEntryImportJobDao().findInProgressJobsByContentEntryUidAsync(
            contentEntryUid
        )
        call.response.header("cache-control", "no-store")
        call.respondText(
            contentType = ContentType.Application.Json,
            text = json.encodeToString(
                ListSerializer(ContentEntryImportJobProgress.serializer()),
                inProgressJobs
            ),
        )
    }

    get("cancel") {
        val jobUid = call.request.queryParameters["jobUid"]?.toLong() ?: 0
        val accountPersonUid = call.request.queryParameters["accountPersonUid"]?.toLong() ?: 0
        try {
            val (fromNode, auth) = requireRemoteNodeIdAndAuth()

            cancelImportContentEntryServerUseCase(call).invoke(
                cjiUid = jobUid,
                accountPersonUid = accountPersonUid,
                remoteNodeId = fromNode,
                nodeAuth = auth,
            )
            call.response.header("cache-control", "no-store")
            call.respond(HttpStatusCode.OK, "")
        }catch(e: Throwable) {
            Napier.w("CancelImportContentEntryServer: exception with cancel request", e)
        }
    }

    get("dismissError") {
        val jobUid = call.request.queryParameters["jobUid"]?.toLong() ?: 0

        requireRemoteNodeIdAndAuth()
        dbFn(call).contentEntryImportJobDao().updateErrorDismissed(jobUid, true)
        call.response.header("cache-control", "no-store")
        call.respond(HttpStatusCode.OK, "")
    }
}