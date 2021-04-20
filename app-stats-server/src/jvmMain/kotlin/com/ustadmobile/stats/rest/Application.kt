package com.ustadmobile.stats.rest

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.stats.db.StatsDatabase
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.statsModule(){

    install(ContentNegotiation) {
        gson {
            register(ContentType.Application.Json, GsonConverter())
            register(ContentType.Any, GsonConverter())
        }
    }

    val db = DatabaseBuilder.databaseBuilder(Any(),
            StatsDatabase::class, "StatsDatabase").build()

    install(Routing) {
        StatsRoute(db)
    }


}