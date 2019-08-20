package com.ustadmobile.lib.annotationprocessor.core

import com.google.gson.Gson
import db2.ExampleDatabase2
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import io.ktor.routing.Route
import com.ustadmobile.door.*
import db2.ExampleDao2Route
import db2.ExampleEntity2
import db2.ExampleSyncableEntity
import db2.ExampleDatabase2SyncDao_JdbcKt
import db2.ExampleSyncableDaoRoute
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.client.response.HttpResponse
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.request.header
import io.ktor.response.header
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.util.url
import org.junit.After
import org.junit.Assert
import java.util.concurrent.TimeUnit

class TestDbRoute  {

    lateinit var exampleDb: ExampleDatabase2

    lateinit var server: ApplicationEngine

    @Before
    fun setup() {
        exampleDb = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "db1").build()
        exampleDb.clearAllTables()

        val gson = Gson()
        server = embeddedServer(Netty, 8089) {
            install(ContentNegotiation) {
                register(ContentType.Application.Json, GsonConverter())
                register(ContentType.Any, GsonConverter())
            }

            val syncDao = ExampleDatabase2SyncDao_JdbcKt(exampleDb)
            install(Routing) {
                ExampleDao2Route(exampleDb.exampleDao2(), exampleDb, gson)
                ExampleSyncableDaoRoute(exampleDb.exampleSyncableDao(), exampleDb, gson, syncDao)
            }
        }

        server.start()
    }

    @After
    fun tearDown() {
        server.stop(0, 10, TimeUnit.SECONDS)
    }

    @Test
    fun givenDataInsertedOnPost_whenGetByUidCalled_thenShouldReturnSameObject() = runBlocking {
        val httpClient = HttpClient() {
            install(JsonFeature)
        }

        val exampleEntity2 = ExampleEntity2(name = "bob", someNumber =  5L)

        val requestBuilder = HttpRequestBuilder()
        requestBuilder.body = exampleEntity2
        requestBuilder.url("http://localhost:8089/ExampleDao2/insertAndReturnId")
        requestBuilder.contentType(ContentType.Application.Json)

        exampleEntity2.uid = httpClient.post<Long>(requestBuilder)

        val entityFromServer = httpClient.get<ExampleEntity2>(
                "http://localhost:8089/ExampleDao2/findByUid?uid=${exampleEntity2.uid}")
        assertEquals(exampleEntity2, entityFromServer, "Entity from server is retrieved OK")

        httpClient.close()
    }

    @Test
    fun givenSyncableEntityInsertedOnServer_whenReceiptAcknowledged_thenNextRequestShouldReturnEmptyList() = runBlocking {
        val httpClient = HttpClient() {
            install(JsonFeature)
        }
        val exampleSyncableEntity = ExampleSyncableEntity(esMcsn = 1, esNumber =  42)
        exampleSyncableEntity.esUid = exampleDb.exampleSyncableDao().insert(exampleSyncableEntity)

        val firstGetListResponse =  httpClient.get<HttpResponse>("http://localhost:8089/ExampleSyncableDao/findAll") {
            header("X-nid", 1)
        }
        val reqId = firstGetListResponse.headers.get("X-reqid")!!.toInt()
        val firstGetList = firstGetListResponse.receive<List<ExampleSyncableEntity>>()
        httpClient.get<Unit>("http://localhost:8089/ExampleSyncableDao/_updateExampleSyncableEntity_trkReceived?reqId=$reqId")

        val secondGetList = httpClient.get<List<ExampleSyncableEntity>>("http://localhost:8089/ExampleSyncableDao/findAll") {
            header("X-nid", 1)
        }

        Assert.assertEquals("First list has one item", 1, firstGetList.size)
        Assert.assertEquals("First entity in list is the item inserted", exampleSyncableEntity.esUid,
                firstGetList[0].esUid)
        Assert.assertTrue("Second response is empty after receipt was acknowledged",
                secondGetList.isEmpty())
    }

    @Test
    fun givenSyncableEntityInsertedOnServer_whenReceiptIsNotAcknowledged_thenNextRequestShouldReturnSameListAgain() = runBlocking {
        val httpClient = HttpClient() {
            install(JsonFeature)
        }
        val exampleSyncableEntity = ExampleSyncableEntity(esMcsn = 1, esNumber =  42)
        exampleSyncableEntity.esUid = exampleDb.exampleSyncableDao().insert(exampleSyncableEntity)

        val firstGetListResponse = httpClient.get<HttpResponse> {
            url{
                takeFrom("http://localhost:8089/")
                path("ExampleSyncableDao", "findAll")
                parameter("x", 1)
            }
            header("X-nid", 1)
        }

        val firstGetList = firstGetListResponse.receive<List<ExampleSyncableEntity>>()

        val secondGetList = httpClient.get<List<ExampleSyncableEntity>>("http://localhost:8089/ExampleSyncableDao/findAll") {
            header("X-nid", 1)
        }

        Assert.assertEquals("First list has one item", 1, firstGetList.size)
        Assert.assertEquals("First entity in list is the item inserted", exampleSyncableEntity.esUid,
                firstGetList[0].esUid)
        Assert.assertEquals("Second response has one item when response not acknowledged",
                1, secondGetList.size)
    }



}