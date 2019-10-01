package com.ustadmobile.lib.rest

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.util.parseRangeRequestHeader
import com.ustadmobile.port.sharedse.impl.http.RangeInputStream
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream
import javax.naming.InitialContext


fun Route.ContainerMountRoute(db: UmAppDatabase) {

    route("ContainerMount"){
        get("/{containerUid}/{paths...}") {
            val containerUid = call.parameters["containerUid"]?.toLong() ?: 0L
            val pathInContainer = call.parameters.getAll("paths")?.joinToString("/") ?: ""

            val iContext = InitialContext()
            val containerDirPath = iContext.lookup("java:/comp/env/ustadmobile/app-ktor-server/containerDirPath") as String
            val containerDir = File(containerDirPath)
            containerDir.mkdirs()

            val container = db.containerDao.findByUid(containerUid)
            if(container != null){


                val containerManager = ContainerManager(container, db, db, containerDir.absolutePath)
                val entryWithContainerEntryFile  = containerManager.getEntry(pathInContainer)

                if(entryWithContainerEntryFile!= null){

                    val entryFile = entryWithContainerEntryFile.containerEntryFile!!

                    val rangeHeader = call.request.header("Range")?: "bytes=0-"
                    val rangeResponse = parseRangeRequestHeader(rangeHeader, entryFile.ceTotalSize)
                    call.response.header("X-Content-Length-Uncompressed", entryFile.ceTotalSize.toString())
                    var inputStream: InputStream = RangeInputStream(FileInputStream(File(entryFile.cefPath!!)), rangeResponse.fromByte, rangeResponse.toByte)

                    if(entryFile.compression == ContainerEntryFile.COMPRESSION_GZIP){
                        inputStream = GZIPInputStream(inputStream)
                    }
                    call.respondBytes(inputStream.readBytes(), UMFileUtil.getContentType(pathInContainer), HttpStatusCode(rangeResponse.statusCode,""))

                }else{
                    call.respond(HttpStatusCode.NotFound, "No such file in specified container${containerManager.allEntries.size}")
                }

            }else{
                call.respond(HttpStatusCode.NotFound, "No such container: $containerUid")
            }
        }
    }

}

