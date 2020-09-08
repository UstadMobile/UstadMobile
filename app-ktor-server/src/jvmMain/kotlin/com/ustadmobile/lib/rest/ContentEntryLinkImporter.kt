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