package com.ustadmobile.lib.rest

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao

fun Application.UmAppDatabaseModule(db: UmAppDatabase) {
    install(Routing) {
        DaoName(db.contentEntryDao)
    }
}


fun Route.DaoName(dao: ContentEntryDao) {

    route("/DaoName") {
        get("methodName") {
            call.respond("blah")
        }
    }

}


fun main(args: Array<String>) {
    val db = UmAppDatabase.getInstance(Any())
    embeddedServer(Netty, 8080, module = { UmAppDatabaseModule(db) })
}