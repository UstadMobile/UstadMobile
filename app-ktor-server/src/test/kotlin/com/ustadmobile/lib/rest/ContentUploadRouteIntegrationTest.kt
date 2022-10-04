package com.ustadmobile.lib.rest

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.door.ext.DoorTag
import io.ktor.server.application.*
import io.ktor.serialization.gson.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.server.application.install
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
import io.ktor.server.routing.*
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
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.server.config.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.runBlocking
import org.kodein.di.ktor.di

class ContentUploadRouteIntegrationTest {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    private lateinit var tempEpubFile: File

    private lateinit var mockContainerStorageManager: ContainerStorageManager

    private lateinit var di: DI

    private lateinit var endpointScope: EndpointScope

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

        this::class.java.getResourceAsStream("/com/ustadmobile/core/contentformats/epub/test.epub")!!
            .writeToFile(tempEpubFile)

        endpointScope = EndpointScope()
        di = DI {
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
    }

    private fun testContentUploadApplication(block: ApplicationTestBuilder.() -> Unit) {
        testApplication {
            environment {
                config = MapApplicationConfig("ktor.environment" to "test")
            }

            application {
                install(ContentNegotiation) {
                    gson {
                        register(ContentType.Application.Json, GsonConverter())
                        register(ContentType.Any, GsonConverter())
                    }
                }

                di {
                    extend(di)
                }

                routing {
                    ContentUploadRoute()
                }
            }

            block()
        }
    }

    @Test
    fun givenRequestWithValidContent_whenUploaded_thenShouldSaveAsTempFileAndReturnMetaData() = testContentUploadApplication {
        runBlocking {
            val response = client.post("/contentupload/upload") {
                setBody(MultiPartFormDataContent(
                    formData {
                         append("test.", FileInputStream(tempEpubFile).readBytes(), Headers.build {
                             append(HttpHeaders.ContentDisposition, ContentDisposition.File
                                 .withParameter(ContentDisposition.Parameters.Name, "test.")
                                 .withParameter(ContentDisposition.Parameters.FileName, "test.epub").toString())
                             append(HttpHeaders.ContentType, "application/epub+zip")
                         })
                    },
                    boundary = UUID.randomUUID().toString()
                ))
            }


            val responseStr: String = response.body()
            val metadataResult = Json.decodeFromString(MetadataResult.serializer(), responseStr)
            val uploadedUri = metadataResult.entry.sourceUrl
            val uploadPath = uploadedUri?.substringAfter(MetadataResult.UPLOAD_TMP_LOCATOR_PREFIX)

            val uploadTmpDir: File by di.on(Endpoint("localhost"))
                .instance(tag = DiTag.TAG_FILE_UPLOAD_TMP_DIR)
            val uploadedFile = File(uploadTmpDir, uploadPath!!)
            Assert.assertArrayEquals("File tmp content is the same",
                tempEpubFile.readBytes(), uploadedFile.readBytes())
            Assert.assertEquals("Metadata got expected content entry title",
                "ರುಮ್ನಿಯಾ", metadataResult.entry.title)

        }
    }

}