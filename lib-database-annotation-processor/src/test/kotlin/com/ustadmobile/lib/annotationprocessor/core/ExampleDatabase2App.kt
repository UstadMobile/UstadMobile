package com.ustadmobile.lib.annotationprocessor.core

import com.google.gson.Gson
import db2.ExampleDatabase2
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.routing.Routing
import db2.ExampleDatabase2_KtorRoute
import com.ustadmobile.door.DatabaseBuilder
import db2.ExampleEntity2
import io.ktor.application.call
import io.ktor.features.CORS
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.response.respond
import io.ktor.routing.get

private val serverDb = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "ExampleDatabase2")
    .build() as ExampleDatabase2

fun Application.ExampleDatabase2App(devMode: Boolean = true) {

    if(serverDb.exampleDao2().findByUid(5000L) == null) {
        serverDb.exampleDao2().insertAndReturnId(ExampleEntity2(uid = 5000L, name = "Initial Entry"))
    }


    install(ContentNegotiation) {
        gson {
            register(ContentType.Application.Json, GsonConverter())
            register(ContentType.Any, GsonConverter())
        }
    }

    if(devMode) {
        HttpHeaders.AccessControlRequestMethod
        install(CORS) {
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            method(HttpMethod.Put)
            anyHost()
        }

    }

    val gson = Gson()
    install(Routing) {
        ExampleDatabase2_KtorRoute(serverDb, gson)
        get("ExampleDatabase2/clearAllTables") {
            serverDb.clearAllTables()
            call.respond("OK - cleared")
        }
    }
}