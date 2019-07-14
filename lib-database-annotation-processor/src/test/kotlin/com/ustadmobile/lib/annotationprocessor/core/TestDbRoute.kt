package com.ustadmobile.lib.annotationprocessor.core

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
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.server.engine.ApplicationEngine
import org.junit.After
import java.util.concurrent.TimeUnit

class TestDbRoute  {

    lateinit var exampleDb: ExampleDatabase2

    lateinit var server: ApplicationEngine

    @Before
    fun setup() {
        exampleDb = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "db1").build()
        exampleDb.clearAllTables()

        server = embeddedServer(Netty, 8089) {
            install(ContentNegotiation) {
                register(ContentType.Application.Json, GsonConverter())
                register(ContentType.Any, GsonConverter())
            }

            install(Routing) {
                ExampleDao2Route(exampleDb.exampleDao2(), exampleDb)
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
}