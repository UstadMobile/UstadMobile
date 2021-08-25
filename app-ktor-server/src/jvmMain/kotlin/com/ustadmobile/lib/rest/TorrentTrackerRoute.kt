package com.ustadmobile.lib.rest

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on
import java.io.InputStream

fun Route.TorrentTrackerRoute(){

    get("/announce/"){
        val di : DI by di()
        val tracker: TorrentTracker by di.on(call).instance()
        val infoHash = call.receive<InputStream>().readBytes()
        tracker.addTorrentInfoHash(infoHash)
    }


}