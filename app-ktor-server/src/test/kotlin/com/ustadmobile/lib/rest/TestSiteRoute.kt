package com.ustadmobile.lib.rest

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.di.commonJvmDiModule
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Site
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.get
import io.ktor.serialization.gson.GsonConverter
import io.ktor.serialization.gson.gson
import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on
import org.kodein.di.registerContextTranslator

class TestSiteRoute {

    private lateinit var serverDi: DI

    private lateinit var endpointScope: EndpointScope

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Before
    fun setup() {
        endpointScope = EndpointScope()
        serverDi = DI {
            import(commonJvmDiModule)

            import(commonTestKtorDiModule(endpointScope, temporaryFolder))

            registerContextTranslator { _: ApplicationCall ->
                Endpoint("localhost")
            }
        }
    }

    private fun testSiteApplication(block: ApplicationTestBuilder.(httpClient: HttpClient) -> Unit) {
        testApplication {
            environment {
                config = MapApplicationConfig("ktor.environment" to "test")
            }

            val client = createClient {
                install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                    gson()
                }
            }

            application {
                install(ContentNegotiation) {
                    gson {
                        register(ContentType.Application.Json, GsonConverter())
                        register(ContentType.Any, GsonConverter())
                    }
                }

                di {
                    extend(serverDi)
                }

                routing {
                    SiteRoute()
                }
            }

            block(client)
        }
    }


    @Test
    fun givenAvailableWorkSpace_whenRequested_thenShouldReturnWorkSpaceObject(

    ) = testSiteApplication{ client ->
        val db: UmAppDatabase by serverDi.on(Endpoint("localhost")).instance(tag = DoorTag.TAG_DB)
        val site = Site().apply {
            siteName = "UmTestWorkspace"
            guestLogin = true
            registrationAllowed = true
        }
        site.siteUid = db.siteDao.insert(site)

        val siteReceived: Site = runBlocking {
            client.get("/Site/verify").body()
        }

        Assert.assertEquals("Valid workspace was retrieved",
            siteReceived.siteUid, site.siteUid)
    }
}