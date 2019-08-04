package com.ustadmobile.lib.annotationprocessor.core

import com.google.gson.Gson
import db2.ExampleDatabase2
import db2.ExampleSyncableEntity
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import com.ustadmobile.door.DatabaseBuilder
import db2.ExampleDatabase2SyncDao_JdbcKt
import db2.ExampleSyncableDao_Repo
import db2.ExampleSyncableDaoRoute
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.routing.Routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import com.ustadmobile.door.asRepository
import java.util.concurrent.TimeUnit


class TestDbRepo {

    lateinit var serverDb : ExampleDatabase2


    fun createSyncableDaoServer(db: ExampleDatabase2) = embeddedServer(Netty, 8089) {
        install(ContentNegotiation) {
            register(ContentType.Application.Json, GsonConverter())
            register(ContentType.Any, GsonConverter())
        }

        val syncDao = ExampleDatabase2SyncDao_JdbcKt(db)
        install(Routing) {
            ExampleSyncableDaoRoute(db.exampleSyncableDao(), db, syncDao)
        }
    }


    @Test
    fun givenSyncableEntityDao_whenGetSyncableListCalled_shouldMakeHttpRequestAndInsertResult() {
        val mockServer = MockWebServer()
        val httpClient = HttpClient() {
            install(JsonFeature)
        }

        val firstResponseList = listOf(ExampleSyncableEntity(esUid = 42, esMcsn = 5))
        mockServer.enqueue(MockResponse()
                .setBody(Gson().toJson(firstResponseList))
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("X-reqid", 50))
        mockServer.enqueue(MockResponse()
                .setResponseCode(204)
                .setBody(""))
        mockServer.start()

        val db = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "db1").build()
        db.clearAllTables()
        val dbSyncDao = ExampleDatabase2SyncDao_JdbcKt(db)

        val clientNodeId = 5
        val repo = ExampleSyncableDao_Repo(db.exampleSyncableDao(), httpClient, clientNodeId,
                mockServer.url("/").toString(), dbSyncDao)
        val repoResult = repo.findAll()

        val firstRequest = mockServer.takeRequest()
        Assert.assertEquals("First http call was to get list", "/ExampleSyncableDao/findAll",
                firstRequest.path)
        Assert.assertEquals("After list was received from server, it was inserted into local db",
                firstResponseList[0], db.exampleSyncableDao().findByUid(firstResponseList[0].esUid))
        Assert.assertEquals("Request contained client id", clientNodeId,
                firstRequest.getHeader("X-nid").toInt())

        val secondRequest = mockServer.takeRequest()
        Assert.assertEquals("Repo made request to acknowledge receipt of entities",
                "/ExampleSyncableDao/_updateExampleSyncableEntityTrackerReceived?reqId=50",
                secondRequest.path)
    }

    @Test
    fun givenMasterServer_whenRepoGetMethodIsCalled_thenEntityIsCopieToLocalDb() {
        val serverDb = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "ExampleDatabase2")
                .build() as ExampleDatabase2
        val clientDb = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "db1")
                .build() as ExampleDatabase2
        serverDb.clearAllTables()
        clientDb.clearAllTables()
        val server = createSyncableDaoServer(serverDb)
        server.start(wait = false)

        val exampleSyncableEntity = ExampleSyncableEntity(esNumber = 42)
        exampleSyncableEntity.esUid = serverDb.exampleSyncableDao().insert(exampleSyncableEntity)

        val httpClient = HttpClient() {
            install(JsonFeature)
        }

        val clientRepo = clientDb.asRepository<ExampleDatabase2>("http://localhost:8089/", "token", httpClient)
            as ExampleDatabase2

        val entityFromServer = clientRepo.exampleSyncableDao().findByUid(exampleSyncableEntity.esUid)
        Assert.assertNotNull("Entity came back from server using repository", entityFromServer)

        httpClient.close()
        server.stop(0, 10, TimeUnit.SECONDS)
    }




}