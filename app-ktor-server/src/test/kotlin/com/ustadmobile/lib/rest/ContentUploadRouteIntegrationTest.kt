package com.ustadmobile.lib.rest

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.door.ext.DoorTag
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.kodein.di.ktor.DIFeature
import io.ktor.application.install
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.catalog.contenttype.EpubTypePluginCommonJvm
import com.ustadmobile.core.contentjob.ContentPluginManager
import com.ustadmobile.core.contentjob.DummyContentPluginUploader
import com.ustadmobile.core.contentjob.MetadataResult
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import com.ustadmobile.core.impl.di.commonJvmDiModule
import org.junit.Before
import org.junit.Test
import java.io.File
import com.ustadmobile.door.ext.writeToFile
import io.ktor.http.content.*
import io.ktor.routing.*
import java.util.UUID
import java.io.FileInputStream
import io.ktor.utils.io.streams.asInput
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.kodein.di.*
import org.kodein.di.ktor.closestDI
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.networkmanager.ConnectivityLiveData
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.ext.toDoorUri
import org.mockito.kotlin.mock
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

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

                bind<ContentPluginManager>() with scoped(EndpointScope.Default).singleton {
                    ContentPluginManager(listOf(
                        EpubTypePluginCommonJvm(Any(), context, di,
                        DummyContentPluginUploader())))
                }

                bind<ConnectivityLiveData>() with scoped(EndpointScope.Default).singleton {
                    val db: UmAppDatabase = on(context).instance(tag = DoorTag.TAG_DB)
                    ConnectivityLiveData(db.connectivityStatusDao.statusLive())
                }

                bind<File>(tag = DiTag.TAG_FILE_UPLOAD_TMP_DIR) with scoped(EndpointScope.Default).singleton {
                    temporaryFolder.newFolder()
                }
                registerContextTranslator { _: ApplicationCall ->
                    Endpoint("localhost")
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
            val metadataResult = Json.decodeFromString(MetadataResult.serializer(), responseStr)
            val uploadedUri = metadataResult.entry.sourceUrl
            val uploadPath = uploadedUri?.substringAfter(MetadataResult.UPLOAD_TMP_LOCATOR_PREFIX)

            val uploadTmpDir: File by di.on(Endpoint("localhost")).instance(tag = DiTag.TAG_FILE_UPLOAD_TMP_DIR)
            val uploadedFile = File(uploadTmpDir, uploadPath!!)
            Assert.assertArrayEquals("File tmp content is the same",
                tempEpubFile.readBytes(), uploadedFile.readBytes())
            Assert.assertEquals("Metadata got expected content entry title",
                "ರುಮ್ನಿಯಾ", metadataResult.entry.title)
        }
    }


}