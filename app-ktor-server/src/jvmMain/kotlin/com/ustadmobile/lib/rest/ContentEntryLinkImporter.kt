package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.contentscrapers.googledrive.GoogleFile
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.port.sharedse.contentformats.ContentTypeUtil.GFOLDER
import com.ustadmobile.port.sharedse.contentformats.ImportedContentEntryMetaData
import com.ustadmobile.port.sharedse.contentformats.extractContentEntryMetadataFromFile
import com.ustadmobile.port.sharedse.contentformats.mimeTypeSupported
import io.ktor.application.call
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpStatement
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import org.apache.commons.io.FileUtils
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

fun Route.ContentEntryLinkImporter() {


    route("import") {

        post("validateLink") {

            val db: UmAppDatabase by di().on(call).instance(tag = DoorTag.TAG_DB)
            val url = call.receive<String>()

            var tempDir = File.createTempFile("folder", "")
            tempDir.mkdir()
            val contentFile = File(tempDir, "url")

            if (url.startsWith("https://drive.google.com/")) {

                var fileIdLookUp = ""
                if (url.startsWith("https://drive.google.com/file/d/")) {
                    fileIdLookUp = url.substringAfter("https://drive.google.com/file/d/")
                } else if (url.startsWith("https://drive.google.com/drive/folders/")) {
                    fileIdLookUp = url.substringAfter("https://drive.google.com/drive/folders/")
                }

                val hasChar = fileIdLookUp.firstOrNull { it == '/' || it == '?' }
                val fileId = if (hasChar == null) fileIdLookUp else fileIdLookUp.substringBefore(hasChar)
                val apiCall = "https://www.googleapis.com/drive/v3/files/$fileId"

                defaultHttpClient().get<HttpStatement>(apiCall) {
                    parameter("key", "AIzaSyCoVemuuYfb3zT3Qe-CuCjATKPDVbmSzO0")
                    parameter("fields", "id,modifiedTime,name,mimeType,description,thumbnailLink")
                }.execute() {

                    val status = it.status

                    if (status.value == 404) {
                        // Link is not shareable or not found
                        this.call.respond(HttpStatusCode.BadRequest, "Google File/Folder not found")
                        return@execute
                    }

                    val data = it.receive<GoogleFile>()
                    val mimeType = data.mimeType ?: ""

                    val supported = mimeTypeSupported.find { fileMimeType -> fileMimeType == mimeType }
                    when {
                        supported != null -> {

                            defaultHttpClient().get<HttpStatement>(apiCall) {
                                parameter("alt", "media")
                                parameter("key", "AIzaSyCoVemuuYfb3zT3Qe-CuCjATKPDVbmSzO0")
                            }.execute() { fileResponse ->

                                val stream = fileResponse.receive<InputStream>()
                                FileUtils.writeByteArrayToFile(contentFile, stream.readBytes())

                                val metadata = extractContentEntryMetadataFromFile(contentFile.absolutePath, db)
                                if (metadata == null) {
                                    call.respond(HttpStatusCode.BadRequest, "Unsupported")
                                } else {
                                    // so contentEntryEdit calls downloadLink instead of file
                                    metadata.fileUri = apiCall
                                    call.respond(metadata)
                                }
                            }

                        }
                        mimeType == "application/vnd.google-apps.folder" -> {

                            val entry = ContentEntryWithLanguage().apply {
                                contentFlags = ContentEntry.FLAG_IMPORTED
                                contentTypeFlag = ContentEntry.TYPE_COLLECTION
                                licenseType = ContentEntry.LICENSE_TYPE_OTHER
                                title = data.name
                                description = data.description
                                thumbnailUrl = data.thumbnailLink
                                entryId = data.id
                                leaf = false
                            }

                            call.respond(ImportedContentEntryMetaData(entry, data.mimeType!!, apiCall, GFOLDER))

                        }
                        else -> {
                            this.call.respond(HttpStatusCode.BadRequest, "Unsupported")
                            return@execute
                        }
                    }
                }

            } else {

                val huc: HttpURLConnection = URL(url).openConnection() as HttpURLConnection


                val mimeType = huc.contentType
                val data = huc.inputStream
                FileUtils.writeByteArrayToFile(contentFile, data.readBytes())

                val supported = mimeTypeSupported.find { fileMimeType -> fileMimeType == mimeType }
                if (supported != null) {
                    val metadata = extractContentEntryMetadataFromFile(contentFile.toURI().toString(), db)
                    if (metadata == null) {
                        call.respond(HttpStatusCode.BadRequest, "Unsupported")
                    } else {
                        call.respond(metadata)
                    }
                } else {
                    this.call.respond(HttpStatusCode.BadRequest, "Unsupported")
                }

            }

        }
        


        post("downloadLink") {


        }


    }


}