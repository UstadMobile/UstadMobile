package com.ustadmobile.lib.rest

import com.maxmind.geoip2.DatabaseReader
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DatabaseBuilder
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.kodein.di.bind
import org.kodein.di.ktor.DIFeature
import org.kodein.di.singleton
import java.io.File

class TestCountryRoute {

    lateinit var server: ApplicationEngine

    lateinit var db: UmAppDatabase

    lateinit var httpClient: HttpClient

    @Before
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(Any() ,UmAppDatabase::class, "UmAppDatabase").build()
        db.clearAllTables()
        val countryDbFile = File("country.mmdb")
        FileUtils.copyToFile(javaClass.getResourceAsStream("/country.mmdb"),
                countryDbFile)

        server = embeddedServer(Netty, port = 8097) {

            install(DIFeature) {
                bind<DatabaseReader>() with singleton {
                    DatabaseReader.Builder(countryDbFile).build()
                }
            }
            install(XForwardedHeaderSupport)

            install(Routing) {
                CountryRoute()
            }
        }.start(wait = false)

        httpClient = HttpClient(){
            install(JsonFeature)
        }
    }

    @After
    fun tearDown() {
        server.stop(0, 5000)
        httpClient.close()
    }

    @Test
    fun givenIpaddressByProxy_whenRequested_thenShouldReturnCountryCode() {
        runBlocking {
            val response = httpClient.get<HttpStatement> {
                url{
                    takeFrom("http://localhost:8097")
                    path("country", "code")
                    header("x-forwarded-for","86.96.82.186")
                }
            }.execute()

            val code = response.receive<String>()

            Assert.assertEquals("Country code was retrieved, response code is 200",
                    HttpStatusCode.OK.value, response.status.value)
            Assert.assertEquals("Valid workspace was retrieved",
                    "AE", code)
        }
    }
}