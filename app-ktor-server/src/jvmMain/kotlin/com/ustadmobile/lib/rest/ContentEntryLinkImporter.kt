package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.contentscrapers.abztract.ScraperManager
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import org.apache.http.HttpStatus
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on

@ExperimentalStdlibApi
fun Route.ContentEntryLinkImporter() {

    route("import") {

        post("validateLink") {

            val url = call.receive<String>()
            val scraperManager: ScraperManager by di().on(call).instance()
            val metadata = scraperManager.extractMetadata(url)
            if (metadata == null) {
                call.respond(HttpStatusCode.BadRequest, "Unsupported")
            } else {
                call.respond(metadata)
            }
        }

    }

    post("downloadLink") {

        val parentUid = call.request.queryParameters["parentUid"]?.toLong() ?: 0L
        val contentEntryUid = call.request.queryParameters["contentEntryUid"]?.toLong() ?: 0L
        val url = call.receive<String>()
        val scraperType = call.request.queryParameters["scraperType"] ?: ""

        val scraperManager: ScraperManager by di().on(call).instance()
        scraperManager.start(url, scraperType, parentUid, contentEntryUid)
        call.respond(HttpStatusCode.OK)

    }


}