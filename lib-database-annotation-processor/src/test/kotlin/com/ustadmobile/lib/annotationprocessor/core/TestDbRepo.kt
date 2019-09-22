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
import db2.ExampleDatabase2_KtorRoute
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.routing.Routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import com.ustadmobile.door.asRepository
import org.junit.After
import java.util.concurrent.TimeUnit
import com.ustadmobile.door.DoorDatabaseSyncRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals


class TestDbRepo {

    var serverDb : ExampleDatabase2? = null

    var clientDb: ExampleDatabase2? = null

    var server: ApplicationEngine? = null

    lateinit var httpClient: HttpClient

    fun createSyncableDaoServer(db: ExampleDatabase2) = embeddedServer(Netty, 8089) {
        install(ContentNegotiation) {
            register(ContentType.Application.Json, GsonConverter())
            register(ContentType.Any, GsonConverter())
        }

        val syncDao = ExampleDatabase2SyncDao_JdbcKt(db)
        val gson = Gson()
        install(Routing) {
            ExampleDatabase2_KtorRoute(db, gson)
        }
    }

    fun setupClientAndServerDb() {
        try {
            serverDb = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "ExampleDatabase2")
                    .build() as ExampleDatabase2
            clientDb = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "db1")
                    .build() as ExampleDatabase2
            serverDb!!.clearAllTables()
            clientDb!!.clearAllTables()
            server = createSyncableDaoServer(serverDb!!)
            server!!.start(wait = false)
        }catch(e: Exception) {
            e.printStackTrace()
            throw e
        }

    }

    @Before
    fun createHttpClient(){
        httpClient = HttpClient() {
            install(JsonFeature)
        }
    }

    @After
    fun tearDown() {
        server?.stop(0, 10, TimeUnit.SECONDS)
        httpClient.close()
    }

    @Test
    fun givenSyncableEntityDao_whenGetSyncableListCalled_shouldMakeHttpRequestAndInsertResult() {
        val mockServer = MockWebServer()

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
                mockServer.url("/").toString(), "ExampleDatabase2", dbSyncDao)
        val repoResult = repo.findAll()

        val firstRequest = mockServer.takeRequest()
        Assert.assertEquals("First http call was to get list", "/ExampleDatabase2/ExampleSyncableDao/findAll",
                firstRequest.path)
        Assert.assertEquals("After list was received from server, it was inserted into local db",
                firstResponseList[0].esNumber,
                db.exampleSyncableDao().findByUid(firstResponseList[0].esUid)!!.esNumber)
        Assert.assertEquals("Request contained client id", clientNodeId,
                firstRequest.getHeader("X-nid").toInt())

        val secondRequest = mockServer.takeRequest()
        Assert.assertEquals("Repo made request to acknowledge receipt of entities",
                "/ExampleDatabase2/ExampleSyncableDao/_updateExampleSyncableEntity_trkReceived?reqId=50",
                secondRequest.path)
    }

    @Test
    fun givenEntityCreatedOnMaster_whenClientGetCalled_thenShouldReturnAndBeCopiedToServer() {
        setupClientAndServerDb()
        val exampleSyncableEntity = ExampleSyncableEntity(esNumber = 42)
        exampleSyncableEntity.esUid = serverDb!!.exampleSyncableDao().insert(exampleSyncableEntity)

        val clientRepo = clientDb!!.asRepository<ExampleDatabase2>("http://localhost:8089/", "token",
                httpClient) as ExampleDatabase2

        val entityFromServer = clientRepo.exampleSyncableDao().findByUid(exampleSyncableEntity.esUid)
        Assert.assertNotNull("Entity came back from server using repository", entityFromServer)
        val entityInClientDb = clientDb!!.exampleSyncableDao().findByUid(exampleSyncableEntity.esUid)
        Assert.assertEquals("Entity is in client db and has some number property",
                42, entityInClientDb!!.esNumber)
    }

    @Test
    fun givenEntityUpdatedOnServer_whenClientGetCalled_thenLocalEntityShouldBeUpdated() {
        setupClientAndServerDb()
        val serverDb = this.serverDb!!
        val clientDb = this.clientDb!!
        val exampleSyncableEntity = ExampleSyncableEntity(esNumber = 42)
        exampleSyncableEntity.esUid = serverDb.exampleSyncableDao().insert(exampleSyncableEntity)

        val clientRepo = clientDb.asRepository<ExampleDatabase2>("http://localhost:8089/", "token",
                httpClient) as ExampleDatabase2
        val entityFromServerBeforeChange = clientRepo.exampleSyncableDao()
                .findByUid(exampleSyncableEntity.esUid)

        serverDb.exampleSyncableDao().updateNumberByUid(exampleSyncableEntity.esUid, 43)
        val entityFromServerAfterChange = clientRepo.exampleSyncableDao()
                .findByUid(exampleSyncableEntity.esUid)

        Assert.assertEquals("Got original entity from DAO before change", 42,
                entityFromServerBeforeChange!!.esNumber)

        Assert.assertEquals("After change, got new entity", 43,
                entityFromServerAfterChange!!.esNumber)

        Assert.assertEquals("Copy in database is updated copy", 43,
                clientDb.exampleSyncableDao().findByUid(exampleSyncableEntity.esUid)!!.esNumber)
    }


    @Test
    fun givenEntityCreatedOnServer_whenRepoSyncCalled_thenShouldBePresentOnClient() {
        setupClientAndServerDb()
        val serverDb = this.serverDb!!
        val clientDb = this.clientDb!!
        runBlocking {


            val exampleSyncableEntity = ExampleSyncableEntity(esNumber = 42)
            exampleSyncableEntity.esUid = serverDb.exampleSyncableDao().insert(exampleSyncableEntity)

            val clientRepo = clientDb.asRepository<ExampleDatabase2>("http://localhost:8089/", "token",
                    httpClient) as ExampleDatabase2

            (clientRepo as DoorDatabaseSyncRepository).sync(null)

            Assert.assertNotNull("Entity is in client database after sync",
                    clientDb.exampleSyncableDao().findByUid(exampleSyncableEntity.esUid))
        }
    }

    @Test
    fun givenEntityCreatedOnClient_whenRepoSyncCalled_thenShouldBePresentOnServer() {
        setupClientAndServerDb()
        val serverDb = this.serverDb!!
        val clientDb = this.clientDb!!
        val clientRepo = clientDb.asRepository<ExampleDatabase2>("http://localhost:8089/", "token",
                httpClient) as ExampleDatabase2
        runBlocking {
            val exampleSyncableEntity = ExampleSyncableEntity(esNumber = 42)
            exampleSyncableEntity.esUid = clientRepo.exampleSyncableDao().insert(exampleSyncableEntity)

            (clientRepo as DoorDatabaseSyncRepository).sync(null)

            Assert.assertNotNull("Entity is in client database after sync",
                    serverDb.exampleSyncableDao().findByUid(exampleSyncableEntity.esUid))
        }
    }

    @Test
    fun givenEntityCreatedOnClient_whenUpdatedOnServerAndSyncCalled_thenShouldBeUpdatedOnClient() {
        setupClientAndServerDb()
        val serverDb = this.serverDb!!
        val clientDb = this.clientDb!!
        val clientRepo = clientDb.asRepository<ExampleDatabase2>("http://localhost:8089/", "token",
                httpClient) as ExampleDatabase2
        runBlocking {
            val exampleSyncableEntity = ExampleSyncableEntity(esNumber = 42)
            exampleSyncableEntity.esUid = clientRepo.exampleSyncableDao().insert(exampleSyncableEntity)

            (clientRepo as DoorDatabaseSyncRepository).sync(null)

            val entityOnServerAfterSync = serverDb.exampleSyncableDao()
                    .findByUid(exampleSyncableEntity.esUid)

            val serverRepo= serverDb.asRepository<ExampleDatabase2>("http://localhost/dummy", "token",
                    httpClient) as ExampleDatabase2
            serverRepo.exampleSyncableDao().updateNumberByUid(exampleSyncableEntity.esUid, 52)

            (clientRepo as DoorDatabaseSyncRepository).sync(null)

            Assert.assertNotNull("Entity was synced to server after being created on client",
                    entityOnServerAfterSync)
            Assert.assertEquals("Syncing after change made on server, value on client is udpated",
                    52, clientDb.exampleSyncableDao().findByUid(exampleSyncableEntity.esUid)!!.esNumber)
        }
    }

    @Test
    fun givenEntityCreatedOnServer_whenUpdatedOnClientAndSyncCalled_thenShouldBeUpdatedOnServer() {
        setupClientAndServerDb()
        val serverDb = this.serverDb!!
        val clientDb = this.clientDb!!
        val clientRepo = clientDb.asRepository<ExampleDatabase2>("http://localhost:8089/", "token",
                httpClient) as ExampleDatabase2
        val serverRepo= serverDb.asRepository<ExampleDatabase2>("http://localhost/dummy", "token",
                httpClient) as ExampleDatabase2
        runBlocking {
            val exampleSyncableEntity = ExampleSyncableEntity(esNumber = 42)
            exampleSyncableEntity.esUid = serverRepo.exampleSyncableDao().insert(exampleSyncableEntity)
            (clientRepo as DoorDatabaseSyncRepository).sync(null)

            val entityOnClientAfterSync = clientDb.exampleSyncableDao()
                    .findByUid(exampleSyncableEntity.esUid)


            clientRepo.exampleSyncableDao().updateNumberByUid(exampleSyncableEntity.esUid, 53)

            (clientRepo as DoorDatabaseSyncRepository).sync(null)

            Assert.assertNotNull("Entity was synced to client after being created on server",
                    entityOnClientAfterSync)
            Assert.assertEquals("Syncing after change made on server, value on server is udpated",
                    53, serverDb.exampleSyncableDao().findByUid(exampleSyncableEntity.esUid)!!.esNumber)
        }
    }

    @Test
    fun givenSyncableEntityWithListParam_whenGetCalled_thenShouldBeReturned(){
        setupClientAndServerDb()
        val serverDb = this.serverDb!!
        val clientDb = this.clientDb!!
        val clientRepo = clientDb.asRepository<ExampleDatabase2>("http://localhost:8089/", "token",
                httpClient) as ExampleDatabase2
        val serverRepo= serverDb.asRepository<ExampleDatabase2>("http://localhost/dummy", "token",
                httpClient) as ExampleDatabase2
        val e1 = ExampleSyncableEntity(esNumber = 42)
        var e2 = ExampleSyncableEntity(esNumber = 43)
        e1.esUid = serverRepo.exampleSyncableDao().insert(e1)
        e2.esUid = serverRepo.exampleSyncableDao().insert(e2)

        runBlocking {
            val entitiesFromListParam = clientRepo.exampleSyncableDao().findByListParam(
                    listOf(42, 43))
            assertEquals(2, entitiesFromListParam.size, "Got expected results from list param query")
        }

    }



}