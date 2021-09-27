package com.ustadmobile.lib.rest

import com.ustadmobile.core.util.DiTag
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di
import org.kodein.di.on
import java.io.File

fun Route.TorrentFileRoute(){

    route("containers") {

        get("{containerUid}"){
            val torrentDir: File by closestDI().on(call).instance(tag = DiTag.TAG_TORRENT_DIR)
            val containerUid = call.parameters["containerUid"]
            val torrentFile = File(torrentDir, "$containerUid.torrent")
            call.respondFile(torrentFile)
        }
    }
}