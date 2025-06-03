package com.ustadmobile.lib.rest.domain.report.query

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.account.LearningSpaceScope
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.account.VerifyClientUserSessionUseCase
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportPeriodOption
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.query.GenerateReportQueriesUseCase
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import com.ustadmobile.core.domain.report.query.RunReportUseCaseClientImpl
import com.ustadmobile.core.domain.report.query.RunReportUseCaseDatabaseImpl
import com.ustadmobile.core.impl.di.CommonJvmDiModule
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.NodeIdAuthCache
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.SystemPermission
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.lib.db.entities.UserSession.Companion.STATUS_ACTIVE
import com.ustadmobile.lib.rest.CONF_DBMODE_VIRTUALHOST
import com.ustadmobile.lib.rest.commonTestKtorDiModule
import com.ustadmobile.util.test.ext.insertStatementsPerDay
import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
import kotlin.time.Duration.Companion.seconds

class RunReportRouteIntegrationTest {

    private lateinit var serverDi: DI

    private lateinit var endpointScope: LearningSpaceScope

    private val serverEndpoint = LearningSpace("http://localhost/")

    private val defaultClientNodeId = 43L

    private val defaultClientNodeAuth = "secret"

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    data class RunReportRouteIntegrationContext(
        val httpClient: HttpClient,
        val clientDi: DI,
    )

    @Before
    fun setup() {
        endpointScope = LearningSpaceScope()
        serverDi = DI {
            import(CommonJvmDiModule)

            import(commonTestKtorDiModule(endpointScope))

            bind<Json>() with singleton {
                json
            }

            registerContextTranslator { _: ApplicationCall ->
                serverEndpoint
            }
        }
    }

    private fun testReportRouteApplication(
        block: ApplicationTestBuilder.(context: RunReportRouteIntegrationContext) -> Unit
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

            val clientScope = LearningSpaceScope()
            val clientDi by DI.lazy{
                bind<RunReportUseCase>() with scoped(clientScope).singleton {
                    RunReportUseCaseClientImpl(
                        db = instance(tag = DoorTag.TAG_DB),
                        clientNodeId = defaultClientNodeId,
                        clientNodeAuth = defaultClientNodeAuth,
                        learningSpace = context,
                        httpClient = client,
                        json = json,
                    )
                }

                bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(clientScope).singleton {
                    DatabaseBuilder.databaseBuilder(
                        UmAppDatabase::class, "jdbc:sqlite::memory:", nodeId = defaultClientNodeId
                    ).build()
                }

                bind<UmAppDatabase>(tag = DoorTag.TAG_REPO)
            }

            block(
                RunReportRouteIntegrationContext(
                    httpClient = client,
                    clientDi = clientDi,
                )
            )
        }
    }

    @Test
    fun givenReportExists_whenGetReportClientRuns_thenRetrieves() = testReportRouteApplication { testCtx ->
        val json: Json = serverDi.direct.instance()
        val personUid = 1L

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
                    usClientNodeId = defaultClientNodeId
                    usStatus = STATUS_ACTIVE
                }
            )
            serverDb.systemPermissionDao().upsertAsync(
                SystemPermission(
                    spToPersonUid = personUid,
                    spPermissionsFlag = PermissionFlags.COURSE_LEARNINGRECORD_VIEW
                )
            )
            serverDb.insertStatementsPerDay()
            serverDb.reportDao().insert(report)
        }

        val runReportUseCaseImpl: RunReportUseCase = testCtx.clientDi.on(serverEndpoint).direct
            .instance()

        runBlocking {
            val results = runReportUseCaseImpl(reportRequest).toList()
            println("Test received ${results.size} emissions")
            results.forEachIndexed { index, result ->
                println("Emission $index has ${result.results.size} results")
            }
            assertTrue("Should have at least one non-empty result",
                results.any { it.results.isNotEmpty() })
        }
    }
}