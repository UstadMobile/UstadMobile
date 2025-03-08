package com.ustadmobile.lib.rest.domain.report.query

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.account.VerifyClientUserSessionUseCase
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportPeriodOption
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.query.GenerateReportQueriesUseCase
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import com.ustadmobile.core.domain.report.query.RunReportUseCaseDatabaseImpl
import com.ustadmobile.core.impl.di.CommonJvmDiModule
import com.ustadmobile.core.util.ext.bodyAsDecodedText
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.doorNodeIdHeader
import com.ustadmobile.door.ext.setBodyJson
import com.ustadmobile.door.util.NodeIdAuthCache
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.lib.db.entities.UserSession.Companion.STATUS_ACTIVE
import com.ustadmobile.lib.rest.CONF_DBMODE_VIRTUALHOST
import com.ustadmobile.lib.rest.commonTestKtorDiModule
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di
import org.kodein.di.on
import org.kodein.di.registerContextTranslator
import org.kodein.di.scoped
import org.kodein.di.singleton

class RunReportRouteIntegrationTest {

    private lateinit var serverDi: DI

    private lateinit var endpointScope: EndpointScope

    private val serverEndpoint = Endpoint("localhost")

    @Before
    fun setup() {
        endpointScope = EndpointScope()
        serverDi = DI {
            import(CommonJvmDiModule)

            import(commonTestKtorDiModule(endpointScope))

            bind<Json>() with singleton {
                Json {
                    encodeDefaults = true
                    ignoreUnknownKeys = true
                }
            }

            registerContextTranslator { _: ApplicationCall ->
                serverEndpoint
            }
        }
    }

    private fun testReportRouteApplication(
        block: ApplicationTestBuilder.(httpClient: HttpClient) -> Unit
    ) {
        testApplication {
            environment {
                config = MapApplicationConfig(
                    "ktor.environment" to "test",
                    "ktor.ustad.dbmode" to CONF_DBMODE_VIRTUALHOST,
                )
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

                    bind<RunReportServerUseCase>() with scoped(endpointScope).singleton {
                        RunReportServerUseCase(
                            runReportUseCase = instance(),
                            verifyClientSessionUseCase = instance(),
                            db = instance(tag = DoorTag.TAG_DB),
                        )
                    }

                    bind<RunReportUseCase>() with scoped(endpointScope).singleton {
                        RunReportUseCaseDatabaseImpl(
                            db = instance(tag = DoorTag.TAG_DB),
                            generateReportQueriesUseCase = GenerateReportQueriesUseCase()
                        )
                    }

                    bind<NodeIdAuthCache>() with scoped(endpointScope).singleton {
                        NodeIdAuthCache(db = instance(tag = DoorTag.TAG_DB))
                    }

                    bind<VerifyClientUserSessionUseCase>() with scoped(endpointScope).singleton {
                        VerifyClientUserSessionUseCase(
                            db = instance(tag = DoorTag.TAG_DB),
                            nodeIdAndAuthCache = instance(),
                        )
                    }
                }

                routing {
                    val di: DI by closestDI()
                    val json: Json by di.instance()

                    route("api/report") {
                        RunReportRoute(
                            runReportServerUseCase = { call -> di.on(call).direct.instance() },
                            json = json,
                        )
                    }
                }
            }

            block(client)
        }
    }

    @Test
    fun givenReportExists_whenGetReportClientRuns_thenRetrieves(

    )  = testReportRouteApplication {
        val json: Json = serverDi.direct.instance()
        val personUid = 1L
        val nodeId = 2L
        val nodeAuth = "secret"

        val reportRequest = RunReportUseCase.RunReportRequest(
            reportUid = 42L,
            reportOptions = ReportOptions2(
                xAxis = ReportXAxis.DAY,
                series = listOf(
                    ReportSeries2(
                        reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION
                    )
                ),
                period = ReportPeriodOption.LAST_WEEK.period,
            ),
            accountPersonUid = personUid,
            timeZone = TimeZone.UTC,
        )

        val report = Report(
            reportUid = reportRequest.reportUid,
            reportOptions = json.encodeToString(ReportOptions2.serializer(), reportRequest.reportOptions),
            reportOwnerPersonUid = personUid,
        )


        val serverDb: UmAppDatabase = serverDi.on(serverEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        runBlocking {
            serverDb.userSessionDao().insertSession(
                UserSession().apply {
                    usPersonUid = personUid
                    usClientNodeId = nodeId
                    usStatus = STATUS_ACTIVE
                }
            )
            serverDb.reportDao().insert(report)
        }

        val responseText: String = runBlocking {
            client.post("/api/report/run") {
                doorNodeIdHeader(nodeId, nodeAuth)
                setBodyJson(json, RunReportUseCase.RunReportRequest.serializer(), reportRequest)
            }.bodyAsDecodedText()
        }
        val response = json.decodeFromString(RunReportUseCase.RunReportResult.serializer(), responseText)
        println(response)
    }


}