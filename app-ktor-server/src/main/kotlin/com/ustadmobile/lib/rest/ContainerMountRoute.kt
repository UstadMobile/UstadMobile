package com.ustadmobile.lib.rest

import com.ustadmobile.core.controller.VideoContentPresenterCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.port.sharedse.impl.http.*
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_GZIP
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.util.parseRangeRequestHeader
import com.ustadmobile.port.sharedse.impl.http.RangeInputStream
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.utils.io.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.on
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream

fun Route.ContainerMountRoute() {

    route("ContainerMount"){
        get("/recentContainers"){
            val db: UmAppDatabase by closestDI().on(call).instance(tag = DoorTag.TAG_DB)
            val entryUids = (call.parameters["uids"]?:"").split(",").map { it.toLong()}
            val result = db.containerDao.findRecentContainerToBeMonitoredWithEntriesUid(entryUids)
            call.respond(result)

        }
        head("/{containerUid}/{paths...}"){
            this@route.serve(call, true)
        }

        get("/{containerUid}/{paths...}"){
            this@route.serve(call, false)
        }

        get("/{containerUid}/videoParams"){
            val db: UmAppDatabase by closestDI().on(call).instance(tag = DoorTag.TAG_DB)
            val systemImpl : UstadMobileSystemImpl by closestDI().on(call).instance()
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
                        } else if (fileInContainer == "subtitle.srt" || fileInContainer.lowercase() == "subtitle-english.srt") {

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

                srtLangList.sortedWith { a, b -> when {
                        a > b -> 1
                        a < b -> -1
                        else -> 0
                    }
                }

                if (videoPath.isEmpty() && result.isNotEmpty()) {
                    videoPath = result[0].cePath!!
                }

                srtLangList.add(0, systemImpl.getString(MessageID.no_subtitle, context))
                if (defaultLangName.isNotEmpty()) srtLangList.add(1, defaultLangName)

                call.respond(HttpStatusCode.OK, VideoContentPresenterCommon.VideoParams(videoPath, audioEntry, srtLangList, srtMap))

            }else{
                call.respond(HttpStatusCode.NotFound, "No such container: $containerUid")
            }
        }
    }

}

suspend fun Route.serve(call: ApplicationCall, isHeadRequest: Boolean){
    val db: UmAppDatabase by closestDI().on(call).instance(tag = DoorTag.TAG_DB)
    val containerUid = call.parameters["containerUid"]?.toLong() ?: 0L
    val contentTypeEpub = call.parameters["contentTypeEpub"]?.toBoolean() ?: false
    val pathInContainer = call.parameters.getAll("paths")?.joinToString("/") ?: ""

    if(containerUid == 0L || pathInContainer.isEmpty()){
        call.respond(HttpStatusCode.BadRequest, "containerUid or file path was not found")
        return
    }

    val entryWithContainerEntryFile  = db.containerEntryDao.findByPathInContainer(containerUid, pathInContainer)

    if(entryWithContainerEntryFile!= null){
        val containerEntryFile = entryWithContainerEntryFile.containerEntryFile
        if(containerEntryFile != null){
            val isRangeRequest = call.request.headers.contains(HttpHeaders.Range)
            val fileIsGzipped = containerEntryFile.compression == COMPRESSION_GZIP

            val actualFile = File(containerEntryFile.cefPath as String)
            val rangeHeader = if(isRangeRequest) call.request.header(HttpHeaders.Range) as String else "bytes=0-"
            val rangeResponse = parseRangeRequestHeader(rangeHeader, containerEntryFile.ceTotalSize)

            if(isRangeRequest){
                rangeResponse.responseHeaders.forEach{
                    if(it.key != HttpHeaders.ContentLength && it.key != HttpHeaders.AcceptRanges){
                        call.response.header(it.key, it.value)
                    }
                }
            }

            val eTag = entryWithContainerEntryFile.containerEntryFile?.cefMd5
            if(eTag != null) {
                call.response.header(HttpHeaders.ETag, eTag)
            }

            call.response.header(HttpHeaders.CacheControl, "cache; max-age=${TimeUnit.MINUTES.toSeconds(120)}")

            if(call.request.headers[HttpHeaders.IfNoneMatch] == eTag) {
                call.respond(HttpStatusCode.NotModified)
                return
            }

            val contentType = UMFileUtil.getContentType(pathInContainer)
            val mimeType = "${contentType.contentType}/${contentType.contentSubtype}"

            var inputStream = if(isRangeRequest) {
                RangeInputStream(actualFile.inputStream(), rangeResponse.fromByte, rangeResponse.toByte)
            }else {
                actualFile.inputStream()
            }

            //If the file is Gzipped we need to inflate it. Transfer-encoding is controlled by the
            // engine, so even if we have stored the file gzipped, and the response will be
            // gzipped, it must be inflated and deflated again.
            if(fileIsGzipped){
                inputStream = GZIPInputStream(inputStream)
            }

            if(contentTypeEpub){
                inputStream = EpubContainerFilter(closestDI()).filterResponse(inputStream, mimeType)
            }

            call.respond(object : OutgoingContent.WriteChannelContent() {
                override val contentLength: Long?
                    get() = when {
                        contentTypeEpub -> null
                        isRangeRequest -> rangeResponse.actualContentLength
                        else -> containerEntryFile.ceTotalSize
                    }

                override val contentType = contentType

                override val status = if(isRangeRequest)
                    HttpStatusCode.PartialContent else HttpStatusCode.OK

                override suspend fun writeTo(channel: ByteWriteChannel) {
                    if(!isHeadRequest){
                        inputStream.use {
                            val outBytes = inputStream.readBytes()
                            channel.writeFully(outBytes, 0, outBytes.size)
                        }
                    }
                }
            })
        }else{
            call.respond(HttpStatusCode.NotFound, "No such file in specified in a container, path = $pathInContainer")
        }
    }else{
        call.respond(HttpStatusCode.NotFound, "No such file in specified in a container, path = $pathInContainer")
    }

}

