package com.ustadmobile.lib.rest

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on
import java.io.File

fun Route.DevModeRoute() {

    get("UmAppDatabase/clearAllTables") {
        val db: UmAppDatabase by di().on(call).instance(tag = DoorTag.TAG_DB)
        db.clearAllTables()
        call.respond("OK - cleared")
    }

    get("UmContainer/addContainer") {
        val db: UmAppDatabase by di().on(call).instance(tag = DoorTag.TAG_DB)
        val resourceName = call.request.queryParameters["resource"]
        val entryUid = call.request.queryParameters["entryUid"]
        val contentTye = call.request.queryParameters["type"]
        val mimeType = when (contentTye) {
            "tincan" -> "application/tincan+zip"
            "epub" -> "application/epub+zip"
            else -> null
        }

        if (resourceName == null || entryUid == null || mimeType == null) {
            call.respond(HttpStatusCode.BadRequest,
                    "Invalid request make sure you have included all resource param")
            return@get
        }

        val preparedRes = prepareResources(db, resourceName, contentTye!!, entryUid, mimeType)

        val containerManager = ContainerManager(preparedRes.tmpContainer, db, db,
                preparedRes.tempDir.absolutePath)

        addEntriesFromZipToContainer(preparedRes.tempFile.absolutePath, containerManager)
        val containerUid = preparedRes.tmpContainer.containerUid
        call.respond(containerUid)
    }
}

data class PreparedResource(val tempDir: File, val tempFile: File, val tmpContainer: Container)

private fun prepareResources(db: UmAppDatabase, resourceName: String?, path: String, entryId: String?, mimetype: String): PreparedResource {
    val epubContainer = Container()

    if (entryId != null && entryId.isNotEmpty()) {
        epubContainer.containerContentEntryUid = entryId.toLong()
    }

    val tempFile = File.createTempFile("testFile", "tempFile$entryId")

    epubContainer.cntLastModified = tempFile.lastModified()
    epubContainer.mimeType = mimetype
    epubContainer.containerUid = db.containerDao.insert(epubContainer)

    UmFileUtilSe.extractResourceToFile("/com/ustadmobile/core/contentformats/$path/${resourceName}", tempFile)
    val tempDir = UmFileUtilSe.makeTempDir("testFile", "containerDirTmp")
    return PreparedResource(tempDir, tempFile, epubContainer)
}