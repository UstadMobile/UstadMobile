package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import io.ktor.application.install
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.routing.Routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class TestContainerUploadRoute {

    lateinit var server: ApplicationEngine

    lateinit var db: UmAppDatabase

    private val defaultPort = 8098

    lateinit var tmpFolder: File

    @Before
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, "UmAppDatabase").build()
        db.clearAllTables()


        db.containerEntryFileDao.insertList(listOf(ContainerEntryFile().apply {
            this.cefMd5 = "1"
        }))

        server = embeddedServer(Netty, port = defaultPort) {
            install(ContentNegotiation) {
                gson {
                    register(ContentType.Application.Json, GsonConverter())
                    register(ContentType.Any, GsonConverter())
                }
            }
            install(Routing) {
                ContainerUpload()
            }
        }.start(wait = false)
    }

    @Test
    fun givenAListOfMd5Sums_whenRequestMade_ServerShouldReturnListOfMd5ItDoesntHave() {

        runBlocking {
            val data = defaultHttpClient().post<List<String>> {
                url("http://localhost:$defaultPort/ContainerUpload/checkExistingMd5/")
                body = "1;2;3"
            }

            Assert.assertEquals("list matches", listOf("2", "3"), data)
        }
    }
}