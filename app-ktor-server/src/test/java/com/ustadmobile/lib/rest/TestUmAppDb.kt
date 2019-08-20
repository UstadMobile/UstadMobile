package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.rest.subpack.Db
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test





class TestUmAppDb {

    lateinit var server: ApplicationEngine

    lateinit var db: UmAppDatabase

    @Before
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(Any() ,UmAppDatabase::class, "UmAppDatabase").build()
        db.clearAllTables()
        server = embeddedServer(Netty, port = 8097) {
            install(ContentNegotiation) {
                gson {
                    register(ContentType.Application.Json, GsonConverter())
                    register(ContentType.Any, GsonConverter())
                }
            }

            install(Routing) {
                Db()
                ContainerDownload(db)
            }
        }.start(wait = false)
    }

    @Test
    fun testSubDaoRoute() {
        val httpClient = HttpClient()
        runBlocking {
            val helloStr = httpClient.get<String>("http://localhost:8097/Db/SubDao/hello")
            Assert.assertEquals("Hello", helloStr)
        }
    }

    fun testModule() {
        val db = UmAppDatabase.getInstance(Any())
        db.clearAllTables()
        val contentEntry = ContentEntry("Blah", "blah", true, false)
        contentEntry.contentEntryUid = 42L
        db.contentEntryDao.insert(contentEntry)
        embeddedServer(Netty, 8088, module = { UmAppDatabaseModule(db) }).start(wait = true)
    }

}