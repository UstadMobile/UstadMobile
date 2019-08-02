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


class TestDbRepo {

    @Before
    fun setupMockServer() {

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

}