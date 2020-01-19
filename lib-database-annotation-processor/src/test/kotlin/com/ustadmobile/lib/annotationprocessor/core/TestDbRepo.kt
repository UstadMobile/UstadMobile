package com.ustadmobile.lib.annotationprocessor.core

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ustadmobile.door.*
import db2.ExampleDatabase2
import db2.ExampleSyncableEntity
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import db2.ExampleDatabase2SyncDao_JdbcKt
import db2.ExampleSyncableDao_Repo
import db2.ExampleDatabase2_KtorRoute
import db2.ExampleDatabase2_Repo
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.routing.Routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.junit.After
import java.util.concurrent.TimeUnit
import io.ktor.application.call
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.features.CallLogging
import io.ktor.http.HttpStatusCode
 import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.post
import io.netty.handler.codec.http.HttpResponse
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals


class TestDbRepo {

    var serverDb : ExampleDatabase2? = null

    var clientDb: ExampleDatabase2? = null

    var server: ApplicationEngine? = null

    lateinit var httpClient: HttpClient

    var tmpAttachmentsDir: File? = null

    fun createSyncableDaoServer(db: ExampleDatabase2) = embeddedServer(Netty, 8089) {
        install(ContentNegotiation) {
            register(ContentType.Application.Json, GsonConverter())
            register(ContentType.Any, GsonConverter())
        }

        val syncDao = ExampleDatabase2SyncDao_JdbcKt(db)
        val gson = Gson()
        tmpAttachmentsDir = Files.createTempDirectory("testdbrepoattachments").toFile()
        install(Routing) {
            ExampleDatabase2_KtorRoute(db, gson, tmpAttachmentsDir!!.absolutePath)
        }

        install(CallLogging)
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
        tmpAttachmentsDir?.deleteRecursively()
        tmpAttachmentsDir = null
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
        val dbRepo = db.asRepository<ExampleDatabase2>(Any(), mockServer.url("/").toString(),
                "", httpClient, null)
                .asConnectedRepository<ExampleDatabase2>()

        val clientNodeId = (dbRepo as DoorDatabaseSyncRepository).clientId
        val repo = dbRepo.exampleSyncableDao()
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

        val clientRepo = clientDb!!.asRepository<ExampleDatabase2>(Any(),
                "http://localhost:8089/", "token", httpClient)
                .asConnectedRepository<ExampleDatabase2>()

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

        val clientRepo = clientDb.asRepository<ExampleDatabase2>(Any(), "http://localhost:8089/",
                "token", httpClient)
                .asConnectedRepository<ExampleDatabase2>()

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

            val clientRepo = clientDb.asRepository<ExampleDatabase2>(Any(), "http://localhost:8089/",
                    "token", httpClient)
                    .asConnectedRepository<ExampleDatabase2>()

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
        val clientRepo = clientDb.asRepository<ExampleDatabase2>(Any(),"http://localhost:8089/",
                "token", httpClient).asConnectedRepository<ExampleDatabase2>()
        runBlocking {
            val exampleSyncableEntity = ExampleSyncableEntity(esNumber = 42)
            exampleSyncableEntity.esUid = clientRepo.exampleSyncableDao().insert(exampleSyncableEntity)

            (clientRepo as DoorDatabaseSyncRepository).sync(null)

            Assert.assertNotNull("Entity is in client database after sync",
                    serverDb.exampleSyncableDao().findByUid(exampleSyncableEntity.esUid))
        }
    }

    @Test
    fun givenEntityCreatedOnClientWithUtf8Chars_whenRepoSyncCalled_thenShouldBeCorrectOnServer() {
        setupClientAndServerDb()
        val serverDb = this.serverDb!!
        val clientDb = this.clientDb!!
        val clientRepo = clientDb.asRepository<ExampleDatabase2>(Any(),"http://localhost:8089/",
                "token", httpClient).asConnectedRepository<ExampleDatabase2>()
        val entityName = "سلام"

        runBlocking {
            val exampleSyncableEntity = ExampleSyncableEntity(esNumber =  50, esName = entityName)
            exampleSyncableEntity.esUid = clientRepo.exampleSyncableDao().insert(exampleSyncableEntity)

            (clientRepo as DoorDatabaseSyncRepository).sync(null)

            val entityInServer = serverDb.exampleSyncableDao().findByUid(exampleSyncableEntity.esUid)
            val entityOnClient = clientDb.exampleSyncableDao().findByUid(exampleSyncableEntity.esUid)
            Assert.assertNotNull("Entity found on server", entityInServer)
            Assert.assertEquals("Name was saved correctly on client",
                    entityOnClient!!.esName, entityName)
            Assert.assertEquals("Name matches", entityName, entityInServer!!.esName)
        }
    }

    @Test
    fun givenEntityCreatedOnServerWithUtf8Chars_whenRepoSyncCalled_thenShouldBeCorrectOnClient() {
        setupClientAndServerDb()
        val serverDb = this.serverDb!!
        val clientDb = this.clientDb!!
        val clientRepo = clientDb.asRepository<ExampleDatabase2>(Any(),"http://localhost:8089/",
                "token", httpClient).asConnectedRepository<ExampleDatabase2>()
        val serverRepo= serverDb.asRepository<ExampleDatabase2>(Any(), "http://localhost/dummy",
                "token", httpClient).asConnectedRepository<ExampleDatabase2>()

        val entityName = "سلام"

        runBlocking {
            val exampleSyncableEntity = ExampleSyncableEntity(esNumber =  50, esName = entityName)
            exampleSyncableEntity.esUid = serverRepo.exampleSyncableDao().insert(exampleSyncableEntity)

            val entityFromRepoClient = clientRepo.exampleSyncableDao().findByUid(exampleSyncableEntity.esUid)
            Assert.assertNotNull("Got entity on server via client repo", entityFromRepoClient)
            Assert.assertEquals("Name is encoded correctly", entityName, entityFromRepoClient!!.esName)
        }
    }

    @Test
    fun givenEntityCreatedOnClient_whenUpdatedOnServerAndSyncCalled_thenShouldBeUpdatedOnClient() {
        setupClientAndServerDb()
        val serverDb = this.serverDb!!
        val clientDb = this.clientDb!!
        val clientRepo = clientDb.asRepository<ExampleDatabase2>(Any(), "http://localhost:8089/",
                "token", httpClient).asConnectedRepository<ExampleDatabase2>()
        runBlocking {
            val exampleSyncableEntity = ExampleSyncableEntity(esNumber = 42)
            exampleSyncableEntity.esUid = clientRepo.exampleSyncableDao().insert(exampleSyncableEntity)

            (clientRepo as DoorDatabaseSyncRepository).sync(null)

            val entityOnServerAfterSync = serverDb.exampleSyncableDao()
                    .findByUid(exampleSyncableEntity.esUid)

            val serverRepo= serverDb.asRepository<ExampleDatabase2>(Any(), "http://localhost/dummy",
                    "token", httpClient) as ExampleDatabase2
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
        val clientRepo = clientDb.asRepository<ExampleDatabase2>(Any(),
                "http://localhost:8089/", "token",
                httpClient).asConnectedRepository<ExampleDatabase2>()
        val serverRepo= serverDb.asRepository<ExampleDatabase2>(Any(), "http://localhost/dummy", "token",
                httpClient).asConnectedRepository<ExampleDatabase2>()
        runBlocking {
            val exampleSyncableEntity = ExampleSyncableEntity(esNumber = 42)
            exampleSyncableEntity.esUid = serverRepo.exampleSyncableDao().insert(exampleSyncableEntity)
            (clientRepo as DoorDatabaseSyncRepository).sync(listOf(ExampleSyncableEntity::class))

            val entityOnClientAfterSync = clientDb.exampleSyncableDao()
                    .findByUid(exampleSyncableEntity.esUid)


            clientRepo.exampleSyncableDao().updateNumberByUid(exampleSyncableEntity.esUid, 53)
            (clientRepo as DoorDatabaseSyncRepository).sync(listOf(ExampleSyncableEntity::class))


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
        val clientRepo = clientDb.asRepository<ExampleDatabase2>(Any(), "http://localhost:8089/", "token",
                httpClient).asConnectedRepository<ExampleDatabase2>()
        val serverRepo= serverDb.asRepository<ExampleDatabase2>(Any(), "http://localhost/dummy", "token",
                httpClient).asConnectedRepository<ExampleDatabase2>()
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

    @Test
    fun givenBlankEntityInsertedAndSynced_whenLocallyUpdatedAndSynced_shouldUpdateServer() {
        setupClientAndServerDb()
        val serverDb = this.serverDb!!
        val clientDb = this.clientDb!!
        val clientRepo = clientDb.asRepository<ExampleDatabase2>(Any(),"http://localhost:8089/",
                "token", httpClient).asConnectedRepository<ExampleDatabase2>()
        runBlocking {
            //1. Create a blank entity. Insert it.
            val client1 = ExampleSyncableEntity()
            client1.esUid = clientRepo.exampleSyncableDao().insert(client1)
            //2. Lets sync happen  - Verify blank entity is on server
            (clientRepo as DoorDatabaseSyncRepository).sync(null)
            val server1 = serverDb.exampleSyncableDao().findByUid(client1.esUid)
            Assert.assertEquals("Server got the entity OK", client1.esUid, server1!!.esUid)
            //3. Make changes and update entity.
            client1.esName = "Hello"
            client1.esNumber = 42
            clientRepo.exampleSyncableDao().updateAsync(client1)
            val client2 = clientDb.exampleSyncableDao().findByUid(client1.esUid)
            Assert.assertEquals("Client updated locally OK", "Hello", client2!!.esName)
            //4. Let sync happen - Verify update on server.
            (clientRepo as DoorDatabaseSyncRepository).sync(null)
            val server2 = serverDb.exampleSyncableDao().findByUid(client1.esUid)
            Assert.assertEquals("Name matches", "Hello", server2!!.esName)
        }
    }

    @Test
    fun givenOldClient_whenRequestMade_thenShouldReceive400Forbidden() = runBlocking {
        setupClientAndServerDb()
        var httpStatusErr: HttpStatusCode? = null
        try {
            val response = httpClient.get<HttpResponse>("http://localhost:8089/ExampleDatabase2/ExampleSyncableDao/findAllLive") {
                header("X-nid", 1234)
                header(DoorConstants.HEADER_DBVERSION, 0)
            }
        }catch(e: ClientRequestException) {
            httpStatusErr = e.response.status
        }


        Assert.assertEquals(HttpStatusCode.BadRequest, httpStatusErr)
    }




}