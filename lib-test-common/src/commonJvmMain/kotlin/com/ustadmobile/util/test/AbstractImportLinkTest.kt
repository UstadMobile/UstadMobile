package com.ustadmobile.util.test

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.rest.H5PImportRoute
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.routing.Routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty


abstract class AbstractImportLinkTest {

    fun createServer(db: UmAppDatabase, h5pDownloadFn: (String, Long, String, Long) -> Unit): ApplicationEngine {

        return embeddedServer(Netty, port = 8096) {
            install(ContentNegotiation) {
                gson {
                    register(ContentType.Application.Json, GsonConverter())
                    register(ContentType.Any, GsonConverter())
                }
            }

            install(Routing) {
                H5PImportRoute(db, h5pDownloadFn)
            }
        }.start(wait = false)

    }


    fun createDb(db: UmAppDatabase) {

        var firstEntry = ContentEntry()
        firstEntry.title = "Ustad Mobile"
        firstEntry.sourceUrl = "firstUrl"
        db.contentEntryDao.insert(firstEntry)

        var secondEntry = ContentEntry()
        secondEntry.title = "Ustad Mobile 2"
        secondEntry.sourceUrl = "secondUrl"
        secondEntry.contentEntryUid = -102
        db.contentEntryDao.insert(secondEntry)


    }


}