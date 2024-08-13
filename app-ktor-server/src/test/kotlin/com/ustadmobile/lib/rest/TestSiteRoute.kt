package com.ustadmobile.lib.rest

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.impl.di.CommonJvmDiModule
import com.ustadmobile.lib.db.entities.Site
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
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
import org.kodein.di.ktor.di
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
            import(CommonJvmDiModule)

            import(commonTestKtorDiModule(endpointScope))

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
                    json()
                }
            }

            application {
                install(ContentNegotiation) {
                    json()
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
        val siteReceived: Site = runBlocking {
            client.get("/Site/verify").body()
        }

        Assert.assertEquals("Valid workspace was retrieved",
            1, siteReceived.siteUid)
    }
}