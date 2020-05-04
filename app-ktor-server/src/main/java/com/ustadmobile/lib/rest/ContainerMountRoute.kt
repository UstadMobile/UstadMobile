package com.ustadmobile.lib.rest

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.controller.VideoPlayerPresenterCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.util.parseRangeRequestHeader
import com.ustadmobile.port.sharedse.impl.http.RangeInputStream
import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.request.header
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.head
import io.ktor.routing.route
import io.ktor.utils.io.ByteWriteChannel
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import javax.naming.InitialContext
import kotlin.Comparator


fun Route.ContainerMountRoute(db: UmAppDatabase) {

    route("ContainerMount"){

        get("VideoParams/{containerUid}"){
            val containerUid = call.parameters["containerUid"]?.toLong() ?: 0L
            val container = db.containerDao.findByUidAsync(containerUid)
            if(container != null){
                val result = db.containerEntryDao.findByContainerAsync(containerUid)
                var defaultLangName = ""
                var videoPath = ""
                var audioEntry = ContainerEntryWithContainerEntryFile()
                val srtMap = mutableMapOf<String, String>()
                val srtLangList = mutableListOf<String>()
                for (entry in result) {

                    val fileInContainer = entry.cePath
                    val containerEntryFile = entry.containerEntryFile

                    if (fileInContainer != null && containerEntryFile != null) {
                        if (fileInContainer.endsWith(".mp4") || fileInContainer.endsWith(".webm")) {
                            videoPath = fileInContainer
                        } else if (fileInContainer == "audio.c2") {
                            audioEntry = entry
                        } else if (fileInContainer == "subtitle.srt" || fileInContainer.toLowerCase() == "subtitle-english.srt") {

                            defaultLangName = if (fileInContainer.contains("-"))
                                fileInContainer.substring(fileInContainer.indexOf("-") + 1, fileInContainer.lastIndexOf("."))
                            else "English"
                            srtMap[defaultLangName] = fileInContainer
                        } else {
                            val name = fileInContainer.substring(fileInContainer.indexOf("-") + 1, fileInContainer.lastIndexOf("."))
                            srtMap[name] = fileInContainer
                            srtLangList.add(name)
                        }
                    }
                }

                srtLangList.sortedWith(Comparator { a, b ->
                    when {
                        a > b -> 1
                        a < b -> -1
                        else -> 0
                    }
                })

                if (videoPath.isNullOrEmpty() && result.isNotEmpty()) {
                    videoPath = result[0].cePath!!
                }

                srtLangList.add(0, UstadMobileSystemImpl.instance.getString(MessageID.no_subtitle, context))
                if (defaultLangName.isNotEmpty()) srtLangList.add(1, defaultLangName)

                call.respond(HttpStatusCode.OK, VideoPlayerPresenterCommon.VideoParams(videoPath, audioEntry, srtLangList, srtMap))

            }else{
                call.respond(HttpStatusCode.NotFound, "No such container: $containerUid")
            }

        }

        head("/{containerUid}/{paths...}"){
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
                    val containerEntryFile = entryWithContainerEntryFile.containerEntryFile
                    if(containerEntryFile != null){
                        val actualFile = File(containerEntryFile.cefPath as String)
                        val eTag = Integer.toHexString(("${actualFile.name}${actualFile.lastModified()}${actualFile.length()}").hashCode())
                        call.response.header(HttpHeaders.CacheControl, "cache; max-age=${TimeUnit.MINUTES.toSeconds(60)}")
                        call.response.header(HttpHeaders.ETag, eTag)
                        val contentType = UMFileUtil.getContentType(pathInContainer)
                        call.respond(object : OutgoingContent.NoContent(){
                            override val contentType = contentType
                            override val contentLength = actualFile.length()
                            override val status = HttpStatusCode.OK
                        })
                    }else{
                        call.respond(HttpStatusCode.NotFound, "No such file in specified in a container, path = $pathInContainer")
                    }
                }else{
                    call.respond(HttpStatusCode.NotFound, "No such file in specified in a container, path = $pathInContainer")
                }
            }else{
                call.respond(HttpStatusCode.NotFound, "No such container: $containerUid")
            }
        }


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

                    val isRangeRequest = call.request.headers.contains(HttpHeaders.Range)

                    val containerEntryFile = entryWithContainerEntryFile.containerEntryFile
                    if(containerEntryFile != null){
                        val actualFile = File(containerEntryFile.cefPath as String)

                        val rangeHeader = if(isRangeRequest) call.request.header(HttpHeaders.Range) as String else "bytes=0-"
                        val rangeResponse = parseRangeRequestHeader(rangeHeader, containerEntryFile.ceTotalSize)
                        rangeResponse.responseHeaders.forEach{
                            if(it.key != HttpHeaders.ContentLength && it.key != HttpHeaders.AcceptRanges){
                                call.response.header(it.key, it.value)
                            }
                        }

                        val eTag = Integer.toHexString(("${actualFile.name}${actualFile.lastModified()}${actualFile.length()}").hashCode())
                        call.response.header(HttpHeaders.ETag, eTag)
                        call.response.header(HttpHeaders.CacheControl, "cache; max-age=${TimeUnit.MINUTES.toSeconds(60)}")
                        val ifNonMatch = call.request.headers[HttpHeaders.IfNoneMatch]

                        if(ifNonMatch != null && eTag == ifNonMatch){
                            call.respond(HttpStatusCode.NotModified)
                        }else{

                            val contentType = UMFileUtil.getContentType(pathInContainer)
                            var inputStream: InputStream = if(contentType.contentSubtype.contains("video"))
                                RangeInputStream(actualFile.inputStream(), rangeResponse.fromByte, rangeResponse.toByte)
                            else actualFile.inputStream()

                            if(containerEntryFile.compression == ContainerEntryFile.COMPRESSION_GZIP){
                                inputStream = GZIPInputStream(inputStream)
                            }

                            call.respond(object : OutgoingContent.WriteChannelContent() {
                                override val contentType = contentType
                                override val contentLength = rangeResponse.actualContentLength
                                override val status = if(isRangeRequest)
                                    HttpStatusCode.PartialContent else HttpStatusCode.OK
                                override suspend fun writeTo(channel: ByteWriteChannel) {
                                    inputStream.use {
                                        val outBytes = inputStream.readBytes()
                                        channel.writeFully(outBytes, 0, outBytes.size)
                                    }
                                }
                            })
                        }

                    }else{
                        call.respond(HttpStatusCode.NotFound, "No such file in specified in a container, path = $pathInContainer")
                    }

                }else{
                    call.respond(HttpStatusCode.NotFound, "No such file in specified in a container, path = $pathInContainer")
                }

            }else{
                call.respond(HttpStatusCode.NotFound, "No such container: $containerUid")
            }
        }
    }

}

