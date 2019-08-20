package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_KtorRoute
import com.ustadmobile.door.DatabaseBuilder
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.routing.Routing
import java.io.File
import java.nio.file.Files

private val _restApplicationDb = DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class,
        "UmAppDatabase").build()

fun Application.umRestApplication() {
    install(ContentNegotiation) {
        gson {
            register(ContentType.Application.Json, GsonConverter())
            register(ContentType.Any, GsonConverter())
        }
    }

    install(Routing) {
        ContainerDownload(_restApplicationDb)
        H5PImportRoute(_restApplicationDb) { url: String, entryUid: Long, urlContent: String, containerUid: Long ->
            downloadH5PUrl(_restApplicationDb, url, entryUid, Files.createTempDirectory("h5p").toFile(), urlContent, containerUid)
        }

        LoginRoute(_restApplicationDb)
        UmAppDatabase_KtorRoute(_restApplicationDb, Gson())
    }
}