package com.ustadmobile.lib.rest

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.*
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import java.io.*
import java.util.zip.GZIPInputStream
import javax.naming.InitialContext


fun Route.ContainerMount(db: UmAppDatabase) {

    route("ContainerMount"){
        get("/{containerUid}/{paths...}") {
            val containerUid = call.parameters["containerUid"]?.toLong() ?: 0L
            val pathInContainer = call.parameters.getAll("paths")?.joinToString("/") ?: ""

            val iContext = InitialContext()
            val containerDirPath = iContext.lookup("java:/comp/env/ustadmobile/app-ktor-server/containerDirPath") as String
            val containerDir = File(containerDirPath)
            containerDir.mkdirs()

            println(containerDir.absoluteFile)

            val container = db.containerDao.findByUid(containerUid)
            if(container != null){

                val containerManager = ContainerManager(container, db, db, containerDir.absolutePath)
                val entryWithContainerEntryFile  = containerManager.getEntry(pathInContainer)

                if(entryWithContainerEntryFile!= null){

                    val entryFile = entryWithContainerEntryFile.containerEntryFile!!
                    call.response.header("X-Content-Length-Uncompressed", entryFile.ceTotalSize.toString())
                    var inputStream: InputStream = FileInputStream(File(entryFile.cefPath!!))
                    if(entryFile.compression == ContainerEntryFile.COMPRESSION_GZIP){
                        inputStream = GZIPInputStream(inputStream)
                    }
                    call.respondBytes(inputStream.readBytes(), getContentType(pathInContainer), HttpStatusCode.OK)

                }else{
                    call.respond(HttpStatusCode.NotFound, "No such file in specified container${containerManager.allEntries.size}")
                }

            }else{
                call.respond(HttpStatusCode.NotFound, "No such container: $containerUid")
            }
        }
    }
}

fun getContentType(filePath: String): ContentType {
    val extension = filePath.substring(filePath.lastIndexOf("."))
    val extensionMap = mapOf(
            ".html" to ContentType.Text.Html,".xml" to ContentType.Text.Xml,
            ".css" to ContentType.Text.CSS, ".js" to ContentType.Text.JavaScript,
            ".txt" to ContentType.Text.Html, ".xhtml" to ContentType.Text.Html,
            ".jpg" to ContentType.Image.JPEG,".png" to ContentType.Image.PNG,
            ".gif" to ContentType.Image.GIF, ".mp4" to ContentType.Video.MP4,
            ".mpeg" to ContentType.Video.MPEG)
    return extensionMap[extension] ?: ContentType.Any
}