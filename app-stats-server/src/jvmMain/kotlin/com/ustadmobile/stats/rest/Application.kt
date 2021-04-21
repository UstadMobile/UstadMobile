package com.ustadmobile.stats.rest

import com.google.gson.Gson
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.stats.db.StatsDatabase
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import java.io.File
import java.util.*
import javax.naming.InitialContext
import com.ustadmobile.lib.util.ext.bindDataSourceIfNotExisting

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.statsModule(){

    val gson = Gson()
    val initialContext = InitialContext()

    val conf = environment.config
    val dataDir = File(conf.propertyOrNull("ktor.ustad.datadir")?.getString() ?: "data")
    if(!dataDir.exists())
        dataDir.mkdirs()

    val dbProperties = Properties().apply {
        setProperty("driver", conf.propertyOrNull("ktor.database.driver")?.getString() ?: "org.sqlite.JDBC")
        setProperty("url",
            conf.propertyOrNull("ktor.database.url")?.getString()
                ?: "jdbc:sqlite:data/StatsDatabase.sqlite?journal_mode=WAL&synchronous=OFF&busy_timeout=30000")
        setProperty("user",
            conf.propertyOrNull("ktor.database.user")?.getString() ?: "")
        setProperty("password",
            conf.propertyOrNull("ktor.database.password")?.getString() ?: "")
    }

    initialContext.bindDataSourceIfNotExisting("StatsDatabase", dbProperties)
    install(ContentNegotiation) {
        gson {
            register(ContentType.Application.Json, GsonConverter())
            register(ContentType.Any, GsonConverter())
        }
    }

    val db = DatabaseBuilder.databaseBuilder(Any(),
            StatsDatabase::class, "StatsDatabase").build()

    install(Routing) {
        StatsRoute(db, gson)
    }


}