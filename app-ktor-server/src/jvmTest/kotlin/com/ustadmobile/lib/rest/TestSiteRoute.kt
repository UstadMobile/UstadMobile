package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.Site
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.statement.HttpStatement
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import io.ktor.routing.Routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.junit.Assert

class TestSiteRoute {
    lateinit var server: ApplicationEngine

    lateinit var db: UmAppDatabase

    lateinit var httpClient: HttpClient

    //@Before
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(Any() ,UmAppDatabase::class, "UmAppDatabase").build()
        db.clearAllTables()
        server = embeddedServer(Netty, port = 8097) {
            install(ContentNegotiation) {
                gson {
                    register(ContentType.Application.Json, GsonConverter())
                    register(ContentType.Any, GsonConverter())
                }
            }

            install(Routing) {
                SiteRoute()
            }
        }.start(wait = false)

        httpClient = HttpClient(){
            install(JsonFeature)
        }
    }

    //@After
    fun tearDown() {
        server.stop(0, 5000)
        httpClient.close()
    }

    //@Test
    fun givenAvailableWorkSpace_whenRequested_thenShouldReturnWorkSpaceObject() {
        val site = Site().apply {
            siteName = "UmTestWorkspace"
            guestLogin = true
            registrationAllowed = true
        }
        site.siteUid = db.siteDao.insert(site)

        runBlocking {
            val response = httpClient.get<HttpStatement> {
                url{
                    takeFrom("http://localhost:8097")
                    path("Workspace", "verify")
                }
            }.execute()

            val mWorkSpace = response.receive<Site>()

            Assert.assertEquals("Workspace was retrieved, response code is 200",
                    HttpStatusCode.OK, response.status.value)
            Assert.assertEquals("Valid workspace was retrieved",
                    mWorkSpace.siteUid, site.siteUid)
        }
    }
}