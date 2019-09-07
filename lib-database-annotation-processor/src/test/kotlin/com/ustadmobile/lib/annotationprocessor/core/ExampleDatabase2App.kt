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
import io.ktor.features.DefaultHeaders

private val serverDb = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "ExampleDatabase2")
    .build() as ExampleDatabase2

fun Application.ExampleDatabase2App(devMode: Boolean = true) {

    if(serverDb.exampleDao2().findByUid(42L) == null) {
        serverDb.exampleDao2().insertAndReturnId(ExampleEntity2(uid = 42L, name = "BobJs"))
    }


    install(ContentNegotiation) {
        gson {
            register(ContentType.Application.Json, GsonConverter())
            register(ContentType.Any, GsonConverter())
        }
    }

    if(devMode) {
        install(DefaultHeaders) {
            header("Access-Control-Allow-Origin", "*")
            header("Access-Control-Allow-Headers", "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With")
        }
    }

    val gson = Gson()
    install(Routing) {
        ExampleDatabase2_KtorRoute(serverDb, gson)
    }
}