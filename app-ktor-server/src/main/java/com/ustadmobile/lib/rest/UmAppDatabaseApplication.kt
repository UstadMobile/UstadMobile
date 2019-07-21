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
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive

fun Application.UmAppDatabaseModule(db: UmAppDatabase) {
    install(ContentNegotiation) {
        gson {

        }
    }

    install(Routing) {
        DaoName(db.contentEntryDao)
    }
}

data class Person(val name: String)

fun Route.DaoName(dao: ContentEntryDao) {
    route("/ContentEntryDao") {
        get("findByUidAsync") {
            val person = call.receive<Person>()
            val result = dao.findByUidAsync(call.request.queryParameters["entryUid"]?.toLong() ?: 0L)
            if(result != null) {
                call.respond(result)
            }else {
                call.respond(HttpStatusCode.NoContent, "")
            }
        }
    }
}


fun main(args: Array<String>) {
    val db = UmAppDatabase.getInstance(Any())
    embeddedServer(Netty, 8080, module = { UmAppDatabaseModule(db) })
}

