package com.ustadmobile.stats.rest

import com.google.gson.Gson
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.ext.withUtf8Charset
import com.ustadmobile.lib.db.entities.UstadCentralReportRow
import com.ustadmobile.stats.db.StatsDatabase
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TestStats {


    lateinit var server: ApplicationEngine

    val DEFAULT_SERVER_PORT = 8232

    lateinit var statsDatabase: StatsDatabase

    lateinit var httpClient: HttpClient

    @Before
    fun setup() {
        val gson = Gson()
        statsDatabase = DatabaseBuilder.databaseBuilder(Any(), StatsDatabase::class,
            "StatsDatabase").build()
        server = embeddedServer(Netty, DEFAULT_SERVER_PORT) {
            install(ContentNegotiation) {
                gson {
                    register(ContentType.Application.Json, GsonConverter())
                    register(ContentType.Any, GsonConverter())
                }
            }

            install(Routing) {
                StatsRoute(statsDatabase, gson)
            }
        }
        server.start()

        httpClient = HttpClient() {
            install(JsonFeature)
        }
    }

    @After
    fun tearDown() {
        httpClient.close()
        server.stop(1000, 1000)
    }

    @Test
    fun givenStatsRow_whenPosted_thenShouldBeInDb() {
        println("Hi")
        val rowsToInsert = listOf(UstadCentralReportRow().apply {
            this.disaggregationKey = 42
            this.disaggregationValue = 200
        })

        runBlocking {
            httpClient.post<Unit>("http://localhost:$DEFAULT_SERVER_PORT/receive-stats") {
                body = defaultSerializer().write(rowsToInsert, ContentType.Application.Json.withUtf8Charset())
            }
        }

        val listInDb = statsDatabase.ustadCentralReportRowDao.findByDisaggregationKey(42)
        Assert.assertEquals(200, listInDb.first().disaggregationValue)
    }


}