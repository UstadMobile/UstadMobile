package com.ustadmobile.lib.rest

import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.door.util.NullOutputStream
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.on
import java.io.File
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.rest.ext.dbModeToEndpoint
import com.ustadmobile.core.contentjob.ContentJobManager
import java.io.FileOutputStream
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.DoorObserver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import org.quartz.spi.JobStore
import com.ustadmobile.core.contentjob.UploadResult
import io.ktor.http.*

//The timeout for running an import
const val PLUGIN_IMPORT_TIMEOUT = 15000L

/**
 * This route provides a simple endpoint that will take content files submitted via the web client
 * as 'normal' multipart file uploads and attempt to import them as content.
 *
 * Use as follows
 * POST a multipart request with one file field
 * Returns UploadResult (as JSON)
 *
 */
fun Route.ContentUploadRoute() {
    route("contentupload") {
        post("upload") {
            val db: UmAppDatabase by closestDI().on(call).instance(tag = DoorTag.TAG_DB)
            val repo: UmAppDatabase by closestDI().on(call).instance(tag = DoorTag.TAG_REPO)
            val contentJobManager: ContentJobManager by closestDI().on(call).instance()
            val endpoint = call.application.environment.config.dbModeToEndpoint(call = call)
            val containerStorageManager: ContainerStorageManager by closestDI().on(call).instance()


            val multipartData = call.receiveMultipart()

            var filePartFound = false
            multipartData.forEachPart { part ->
                when(part) {
                    is PartData.FileItem -> {
                        filePartFound = true
                        val fileName = part.originalFileName
                        val tmpFile = File.createTempFile("contentupload-", fileName)
                        try {
                            part.streamProvider().use {
                                it.copyTo(FileOutputStream(tmpFile))
                            }

                            //Create the content job
                            val job = ContentJob().apply {
                                toUri = containerStorageManager.storageList.first().dirUri
                                cjIsMeteredAllowed = true
                                cjUid = db.contentJobDao.insertAsync(this)
                            }

                            val jobItem = ContentJobItem().apply {
                                cjiJobUid = job.cjUid
                                sourceUri = tmpFile.toDoorUri().toString()
                                cjiItemTotal = tmpFile.length()
                                cjiStatus = JobStatus.QUEUED
                                cjiUid = db.contentJobItemDao.insertJobItem(this)
                            }

                            contentJobManager.enqueueContentJob(endpoint, job.cjUid)

                            val completeableDeferred = CompletableDeferred<ContentJobItem>()
                            val observer = DoorObserver<ContentJobItem?> {
                                if(it != null && it.cjiStatus == JobStatus.COMPLETE)
                                    completeableDeferred.complete(it)
                            }

                            db.contentJobItemDao.getJobItemByUidLive(jobItem.cjiUid).observeForever(observer)
                            val completedJobItem = withTimeoutOrNull(PLUGIN_IMPORT_TIMEOUT) {
                                completeableDeferred.await()
                            }

                            if(completedJobItem != null) {
                                call.respond(UploadResult(JobStatus.COMPLETE, completedJobItem.cjiContentEntryUid))
                            }else {
                                call.respond(UploadResult(
                                    completedJobItem?.cjiStatus ?: JobStatus.FAILED, 0)
                                )
                            }
                        }finally {
                            tmpFile.delete()
                        }
                    }
                    else -> {
                        //Do nothing
                    }
                }
            }

            if(!filePartFound) {
                call.respond(HttpStatusCode.BadRequest, "No file found")
            }

        }
    }
}
