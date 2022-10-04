package com.ustadmobile.lib.rest

import io.github.aakira.napier.Napier
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ContentPluginManager
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.getSize
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.createTemporaryDir
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.rest.ext.dbModeToEndpoint
import io.ktor.server.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.withTimeout
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.on
import java.io.File
import java.net.URL


private val IMPORT_LINK_TIMEOUT_DEFAULT = (30 * 1000).toLong()

fun Route.ContentEntryLinkImporter() {

    route("import") {

        post("validateLink") {
            val url = call.request.queryParameters["url"]?: ""
            val di = closestDI()
            val pluginManager: ContentPluginManager by di.on(call).instance()

            ContentJobProcessContext(DoorUri.parse(url),
                    createTemporaryDir("content"), mutableMapOf(), null, di).use { processContext ->
                val metadata: MetadataResult?
                try{
                    metadata = withTimeout(IMPORT_LINK_TIMEOUT_DEFAULT) {
                        pluginManager.extractMetadata(DoorUri.parse(url), processContext)
                    }
                    if (metadata == null) {
                        call.respond(HttpStatusCode.BadRequest, "Unsupported")
                    } else {
                        call.respond(metadata)
                    }
                }catch (e: Exception){
                    Napier.e("Exception extracting metadata to validateLink: $url", e)
                }
           }

        }

        post("downloadLink") {
            val di = closestDI()

            try {
                val parentUid = call.request.queryParameters["parentUid"]?.toLong()

                val url = (call.request.queryParameters["url"]?: "").let {
                    if(it.startsWith(MetadataResult.UPLOAD_TMP_LOCATOR_PREFIX)) {
                        val uploadTmpDir: File = di.direct.on(call).instance(
                            tag = DiTag.TAG_FILE_UPLOAD_TMP_DIR)
                        val uploadTmpPath = it.substringAfter(MetadataResult.UPLOAD_TMP_LOCATOR_PREFIX)
                        File(uploadTmpDir, uploadTmpPath).toDoorUri().toString()
                    }else {
                        it
                    }
                }


                Napier.i("Downloadlink: $url")

                val contentEntry = call.receive<ContentEntryWithLanguage>()
                val pluginId = call.request.queryParameters["pluginId"]?.toInt() ?: 0
                val conversionParams = call.request.queryParameters["conversionParams"]
                Napier.i("contentEntry from client: ${contentEntry.contentEntryUid}")

                val db: UmAppDatabase by closestDI().on(call).instance(tag = DoorTag.TAG_DB)
                val repo: UmAppDatabase by closestDI().on(call).instance(tag = DoorTag.TAG_REPO)
                val containerFolder: File by closestDI().on(call).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)
                val entryFromDb = db.contentEntryDao.findByUid(contentEntry.contentEntryUid)
                val endpoint = call.application.environment.config.dbModeToEndpoint(call = call)
                if (entryFromDb == null) {
                    Napier.i("not synced so using contentEntry from db")
                    repo.contentEntryDao.insertWithReplace(contentEntry)
                }

                val contentJobManager: ContentJobManager by closestDI().on(call).instance()
                val job = ContentJob().apply {
                    toUri = containerFolder.toURI().toString()
                    params = conversionParams
                    cjIsMeteredAllowed = true
                    cjUid = db.contentJobDao.insertAsync(this)
                }
                ContentJobItem().apply {
                    cjiJobUid = job.cjUid
                    sourceUri = DoorUri.parse(url).toString()
                    cjiItemTotal = sourceUri?.let { DoorUri.parse(it).getSize(context, closestDI())  } ?: 0L
                    cjiPluginId = pluginId
                    cjiContentEntryUid = contentEntry.contentEntryUid
                    cjiIsLeaf = contentEntry.leaf
                    cjiParentContentEntryUid = parentUid ?: 0
                    cjiConnectivityNeeded = false
                    cjiStatus = JobStatus.QUEUED
                    cjiUid = db.contentJobItemDao.insertJobItem(this)
                }

                contentJobManager.enqueueContentJob(endpoint, job.cjUid)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                Napier.e("Exception attempting to start scrape", e)
                call.respond(HttpStatusCode.BadRequest, "Unsupported")
            }
        }

    }

}