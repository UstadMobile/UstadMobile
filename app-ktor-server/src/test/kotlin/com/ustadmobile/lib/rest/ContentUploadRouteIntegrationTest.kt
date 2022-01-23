package com.ustadmobile.lib.rest

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.randomUuid
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.kodein.di.ktor.DIFeature
import kotlin.random.Random
import io.ktor.application.install
import com.ustadmobile.core.account.EndpointScope
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import com.ustadmobile.core.impl.di.commonJvmDiModule
import io.ktor.client.request.forms.*
import org.junit.Before
import org.junit.Test
import java.io.File
import com.ustadmobile.door.ext.writeToFile
import io.ktor.http.content.*
import io.ktor.routing.*
import kotlin.io.readBytes
import java.util.UUID
import java.io.FileInputStream
import io.ktor.utils.io.streams.asInput
import kotlinx.serialization.json.Json
import com.ustadmobile.core.contentjob.UploadResult
import org.junit.Assert
import com.ustadmobile.core.db.JobStatus
import org.kodein.di.*
import org.kodein.di.ktor.closestDI
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.ext.toDoorUri
import io.ktor.http.ContentDisposition.Companion.File
import org.mockito.kotlin.mock
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ContentJobManagerJvm
import com.ustadmobile.core.contentjob.ContentPluginManager
import com.ustadmobile.core.networkmanager.ConnectivityLiveData
import com.ustadmobile.lib.rest.ext.databasePropertiesFromSection
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import javax.naming.InitialContext
import com.ustadmobile.lib.util.ext.bindDataSourceIfNotExisting
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import com.ustadmobile.core.catalog.contenttype.EpubTypePluginCommonJvm
import com.ustadmobile.core.contentjob.DummyContentPluginUploader

class ContentUploadRouteIntegrationTest {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    private lateinit var tempEpubFile: File

    private lateinit var mockContainerStorageManager: ContainerStorageManager

    @Before
    fun setup() {
        Napier.base(DebugAntilog())
        tempEpubFile = temporaryFolder.newFile("tempepub.epub")
        mockContainerStorageManager = mock {
            on { storageList }.thenReturn(listOf(
                ContainerStorageDir(
                    temporaryFolder.newFolder("storagetmp").toDoorUri().toString(),
                    "Test Storage", File(".").usableSpace, false)
            ))
        }

        this::class.java.getResourceAsStream("/com/ustadmobile/core/contentformats/epub/test.epub")
            .writeToFile(tempEpubFile)
    }

    private fun <R> withTestContentUpload(testFn: TestApplicationEngine.() -> R) {
        val endpointScope = EndpointScope()

        withTestApplication({
            install(ContentNegotiation) {
                gson {
                    register(ContentType.Application.Json, GsonConverter())
                    register(ContentType.Any, GsonConverter())
                }
            }

            install(DIFeature) {
                import(commonJvmDiModule)
                import(commonTestKtorDiModule(endpointScope, temporaryFolder))
                bind<ContainerStorageManager>() with scoped(endpointScope).singleton {
                    mockContainerStorageManager
                }
                bind<ContentJobManager>() with singleton {
                    ContentJobManagerJvm(di)
                }

                bind<ContentPluginManager>() with scoped(EndpointScope.Default).singleton {
                    ContentPluginManager(listOf(EpubTypePluginCommonJvm(Any(), context, di,
                        DummyContentPluginUploader())))
                }

                bind<Scheduler>() with singleton {
                    val dbProperties = environment.config.databasePropertiesFromSection("quartz",
                        "jdbc:sqlite:data/quartz_test.sqlite?journal_mode=WAL&synchronous=OFF&busy_timeout=30000")
                    InitialContext().apply {
                        bindDataSourceIfNotExisting("quartz_test", dbProperties)
                        initQuartzDb("java:/comp/env/jdbc/quartz_test")
                    }
                    StdSchedulerFactory.getDefaultScheduler().also {
                        it.context.put("di", di)
                    }
                }

                bind<ConnectivityLiveData>() with scoped(EndpointScope.Default).singleton {
                    val db: UmAppDatabase = on(context).instance(tag = DoorTag.TAG_DB)
                    ConnectivityLiveData(db.connectivityStatusDao.statusLive())
                }

                registerContextTranslator { _: ApplicationCall ->
                    Endpoint("localhost")
                }

                onReady {
                    instance<Scheduler>().start()
                    Runtime.getRuntime().addShutdownHook(Thread{
                        instance<Scheduler>().shutdown()
                    })
                }
            }

            routing {
                ContentUploadRoute()
            }
        }) {
            testFn()
        }
    }

    @Test
    fun givenRequestWithValidContent_whenUploaded_thenShouldImportToDatabaseAndReturnContentEntryUid() = withTestContentUpload {
        handleRequest(HttpMethod.Post, "/contentupload/upload") {
            val boundary = UUID.randomUUID().toString()
            addHeader(HttpHeaders.ContentType,
                ContentType.MultiPart.FormData.withParameter("boundary", boundary).toString())
            setBody(boundary, listOf(
                PartData.FileItem({ FileInputStream(tempEpubFile).asInput()}, {},
                    headersOf(HttpHeaders.ContentDisposition to
                        listOf(ContentDisposition.File
                            .withParameter(ContentDisposition.Parameters.Name, "test.")
                            .withParameter(ContentDisposition.Parameters.FileName, "test.epub").toString()
                        ),
                        HttpHeaders.ContentType to listOf("application/epub+zip")
                    )
                )
            ))
        }.apply {
            val di: DI by closestDI()
            val db: UmAppDatabase = di.direct.on(Endpoint("localhost")).instance(tag = DoorTag.TAG_DB)

            val responseStr = response.content!!
            val processResult = Json.decodeFromString(UploadResult.serializer(), responseStr)
            Assert.assertEquals("Upload result is finished", JobStatus.COMPLETE,
                processResult.status)

            val contentEntryUid = processResult.contentEntryUid
            val contentEntryInDb = db.contentEntryDao.findByUid(contentEntryUid)
            Assert.assertEquals("Content entry in db has expected title",
                "ರುಮ್ನಿಯಾ", contentEntryInDb?.title)
        }
    }


}