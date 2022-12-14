package com.ustadmobile.lib.rest

import com.ustadmobile.core.contentjob.*
import io.ktor.server.application.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.on
import java.io.File
import com.ustadmobile.door.ext.toDoorUri
import java.io.FileOutputStream
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.util.systemTimeInMillis
import io.github.aakira.napier.Napier
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import java.util.*

const val UPLOAD_TMP_SUBDIR = "upload-tmp"

/**
 * This route provides a simple endpoint that will take content files submitted via the web client
 * as 'normal' multipart file uploads, store them in a temporary directory, and return the
 * MetadataResult.
 *
 * Use as follows
 * POST a multipart request with one file field
 * Returns MetadataResult (as JSON)
 *
 */
fun Route.ContentUploadRoute() {
    route("contentupload") {
        post("upload") {
            val multipartData = call.receiveMultipart()

            var filePartFound = false
            multipartData.forEachPart { part ->
                when(part) {
                    is PartData.FileItem -> {
                        filePartFound = true
                        val fileName = part.originalFileName
                        val di: DI by closestDI()
                        val uploadTmpDir: File by di.on(call).instance(tag = DiTag.TAG_FILE_UPLOAD_TMP_DIR)
                        val uploadSubDirName = UUID.randomUUID().toString()
                        val uploadSubDir = File(uploadTmpDir, uploadSubDirName).also {
                            it.mkdirs()
                        }

                        val tmpFile = File(uploadSubDir,
                            part.originalFileName ?: "content-upload-${systemTimeInMillis()}")
                        try {
                            withContext(Dispatchers.IO) {
                                part.streamProvider().use { inStream ->
                                    FileOutputStream(tmpFile).use { outStream ->
                                        inStream.copyTo(outStream)
                                    }
                                }
                            }

                            val pluginManager: ContentPluginManager by closestDI().on(call).instance()
                            val metadataResult = pluginManager.extractMetadata(tmpFile.toDoorUri(),
                                ContentJobProcessContext(tmpFile.toDoorUri(), uploadSubDir.toDoorUri(),
                                    mutableMapOf(), null, di))
                            metadataResult.entry.sourceUrl =
                                "${MetadataResult.UPLOAD_TMP_LOCATOR_PREFIX}$uploadSubDirName/${tmpFile.name}"

                            call.respond(metadataResult)
                        }catch(e: Exception) {
                            Napier.e(e) { "ContentUploadRoute: Exception receiving file: $fileName" }
                            call.respond(HttpStatusCode.InternalServerError, "Upload failed $e")
                        }
                    }
                    else -> {
                        //Do nothing
                    }
                }
            }

            if(!filePartFound) {
                call.respond(HttpStatusCode.BadRequest, "No file found.")
            }

        }
    }
}
