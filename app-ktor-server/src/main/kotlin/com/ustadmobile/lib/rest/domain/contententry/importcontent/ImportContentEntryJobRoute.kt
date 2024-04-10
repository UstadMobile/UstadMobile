package com.ustadmobile.lib.rest.domain.contententry.importcontent

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.composites.ContentEntryImportJobProgress
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

fun Route.ImportContentEntryJobStatus(
    json: Json,
    dbFn: (ApplicationCall) -> UmAppDatabase,
) {
    get("importjobs") {
        val contentEntryUid = call.request.queryParameters["contententryuid"]?.toLong() ?: 0
        val db = dbFn(call)
        val inProgressJobs = db.contentEntryImportJobDao.findInProgressJobsByContentEntryUidAsync(
            contentEntryUid
        )

        call.respondText(
            contentType = ContentType.Application.Json,
            text = json.encodeToString(
                ListSerializer(ContentEntryImportJobProgress.serializer()),
                inProgressJobs
            ),
        )
    }
}