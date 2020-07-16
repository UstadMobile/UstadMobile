package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import io.ktor.application.install
import io.ktor.routing.Routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.junit.Before
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class TestContainerUploadRoute {

    lateinit var server: ApplicationEngine

    lateinit var db: UmAppDatabase

    private val defaultPort = 8098

    lateinit var tmpFolder: File

    @Before
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, "UmAppDatabase").build()
        db.clearAllTables()

        server = embeddedServer(Netty, port = defaultPort) {
            install(Routing) {
                ContainerUpload(db)
            }
        }.start(wait = false)

        listOf(ContainerEntryFile().apply {
            this.cefMd5 = "1"
        })
    }

    @Test
    fun givenAFile_upload() {

        val checkmd5sumCon = URL("http://localhost:$defaultPort/ContainerUpload/checkExistingMd5/1;2;3")
                .openConnection() as HttpURLConnection
        checkmd5sumCon.connect()

        val data = String(checkmd5sumCon.inputStream.readBytes())

    }


}