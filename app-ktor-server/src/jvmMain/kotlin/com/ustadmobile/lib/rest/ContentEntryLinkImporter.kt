package com.ustadmobile.lib.rest

import com.github.aakira.napier.Napier
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.contentscrapers.abztract.ScraperManager
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import kotlinx.coroutines.withTimeout
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on


private val IMPORT_LINK_TIMEOUT_DEFAULT = (30 * 1000).toLong()

fun Route.ContentEntryLinkImporter() {

    route("import") {

        post("validateLink") {
            val url = call.request.queryParameters["url"]?: ""
            val scraperManager: ScraperManager by di().on(call).instance()
            var metadata: ImportedContentEntryMetaData? = null
            try{
                metadata = withTimeout(IMPORT_LINK_TIMEOUT_DEFAULT) {
                    scraperManager.extractMetadata(url)
                }
            }catch (e: Exception){
                Napier.e("Exception extracting metadata to validateLink: $url", e)
            }

            if (metadata == null) {
                call.respond(HttpStatusCode.BadRequest, "Unsupported")
            } else {
                call.respond(metadata)
            }
        }

        post("downloadLink") {
            try {
                val parentUid = call.request.queryParameters["parentUid"]?.toLong() ?: UstadView.MASTER_SERVER_ROOT_ENTRY_UID
                val url = call.request.queryParameters["url"]?: ""
                Napier.i("Downloadlink: $url")

                val contentEntry = call.receive<ContentEntryWithLanguage>()
                val scraperType = call.request.queryParameters["scraperType"] ?: ""
                val conversionParams = call.request.queryParameters["conversionParams"]

                val db: UmAppDatabase by di().on(call).instance(tag = DoorTag.TAG_DB)
                val repo: UmAppDatabase by di().on(call).instance(tag = DoorTag.TAG_REPO)
                val entryFromDb = db.contentEntryDao.findByUid(contentEntry.contentEntryUid)
                if (entryFromDb == null) {
                    repo.contentEntryDao.insertWithReplace(contentEntry)
                }

                val scraperManager: ScraperManager by di().on(call).instance()
                scraperManager.start(url, scraperType, parentUid, contentEntry.contentEntryUid,
                        true, conversionParams)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                Napier.e("Exception attempting to start scrape", e)
                call.respond(HttpStatusCode.BadRequest, "Unsupported")
            }
        }

    }

}