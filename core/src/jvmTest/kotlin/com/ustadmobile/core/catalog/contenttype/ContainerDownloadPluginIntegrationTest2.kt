package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.catalog.contenttype.ContainerDownloadTestCommon.Companion.makeContentJobProcessContext
import com.ustadmobile.core.catalog.contenttype.ContainerDownloadTestCommon.Companion.makeDownloadJobAndJobItem
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.ProcessResult
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_KtorRoute
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZip
import com.ustadmobile.core.util.ext.ustadTestCommonModule
import com.ustadmobile.core.util.ext.ustadTestContextModule
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.rest.ContainerDownload
import com.ustadmobile.retriever.Retriever
import com.ustadmobile.retriever.RetrieverBuilder
import com.ustadmobile.util.commontest.ext.assertContainerEqualToOther
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.kodein.di.ktor.DIFeature
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.io.File
import org.junit.After
import org.mockito.kotlin.mock
import org.junit.Assert
import javax.naming.InitialContext
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlin.system.measureTimeMillis

class ContainerDownloadPluginIntegrationTest2 {

    private lateinit var remoteServer: ApplicationEngine

    private lateinit var remoteDi: DI

    private lateinit var remoteDb: UmAppDatabase

    private lateinit var remoteRepo: UmAppDatabase

    private lateinit var clientDi: DI

    private lateinit var clientDb: UmAppDatabase

    private lateinit var clientRepo: UmAppDatabase

    private lateinit var clientEndpoint: Endpoint

    @JvmField
    @Rule
    var tempFolder = TemporaryFolder()

    private lateinit var commonDiModule: DI.Module

    private lateinit var contentEntry: ContentEntry

    private lateinit var container: Container

    private lateinit var epubFile: File

    @Before
    fun setup() {
        Napier.takeLogarithm()
        Napier.base(DebugAntilog())
        commonDiModule = ustadTestCommonModule()


        val remoteEndpointScope = EndpointScope()
        val localhostEndpoint = Endpoint("localhost")
        remoteDi = DI {
            import(commonDiModule)
            import(ustadTestContextModule(tempFolder, "_Server",
                false, remoteEndpointScope))

            registerContextTranslator { _: ApplicationCall ->
                localhostEndpoint
            }
        }
        remoteDb = remoteDi.on(Endpoint("localhost")).direct.instance(tag = UmAppDatabase.TAG_DB)
        remoteRepo = remoteDi.on(Endpoint("localhost")).direct.instance(tag = UmAppDatabase.TAG_REPO)

        remoteServer = embeddedServer(Netty, 8091, configure = {
            requestReadTimeoutSeconds = 600
            responseWriteTimeoutSeconds = 600
        }) {
            install(DIFeature) {
                extend(remoteDi)
            }

            routing {
                ContainerDownload()
                route("UmAppDatabase") {
                    UmAppDatabase_KtorRoute()
                }
            }
        }
        remoteServer.start()

        clientEndpoint = Endpoint("http://localhost:8091/")

        val clientEndpointScope = EndpointScope()
        clientDi = DI {
            import(commonDiModule)
            import(ustadTestContextModule(tempFolder, "_Client",
                true, clientEndpointScope))
            bind<Retriever>() with singleton {
                val retrieverDbName = "retrieverdb_client"
                InitialContext().bindNewSqliteDataSourceIfNotExisting(retrieverDbName)
                RetrieverBuilder.builder("_ustad", instance(), instance(),
                    instance()
                ) {
                    this.dbName = retrieverDbName
                }.build()
            }
        }
        clientDb = clientDi.on(clientEndpoint).direct.instance(tag = UmAppDatabase.TAG_DB)
        clientRepo = clientDi.on(clientEndpoint).direct.instance(tag = UmAppDatabase.TAG_REPO)

        epubFile = tempFolder.newFile()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/contentformats/epub/test.epub")!!
            .writeToFile(epubFile)

        contentEntry = ContentEntry().apply {
            title = "Test Epub"
            contentEntryUid = remoteDb.contentEntryDao.insert(this)
        }

        container = Container().apply {
            containerContentEntryUid = contentEntry.contentEntryUid
            containerUid = remoteDb.containerDao.insert(this)
        }

        val containerDir = DoorUri.parse(remoteDi.direct.on(localhostEndpoint)
            .instance<ContainerStorageManager>().storageList.first().dirUri)

        runBlocking {
            remoteDb.addEntriesToContainerFromZip(
                container.containerUid,
                epubFile.toDoorUri(), ContainerAddOptions(containerDir), Any())
        }
    }

    @After
    fun tearDown() {
        remoteDi.direct.instance<HttpClient>().close()
        remoteServer.stop(500, 500)
    }


    private fun UmAppDatabase.waitForContainerAndContentEntry(contentEntryUid: Long) {
        runBlocking {
            waitUntil(5000, listOf("ContentEntry", "Container")) {
                runBlocking {
                    contentEntryDao.findEntryWithContainerByEntryId(contentEntryUid).let {
                        it?.container != null && (it.container?.fileSize ?: 0) > 0
                    }
                }
            }
        }
    }

    @Test
    fun givenValidContentEntryUid_whenProcessJobCalled_thenShouldDownloadMostRecentContainer() {
        val mockListener = mock<ContentJobProgressListener> { }

        clientDb.waitForContainerAndContentEntry(contentEntry.contentEntryUid)

        println("ContentEntry arrived on client")
        val downloadDir = tempFolder.newFolder()
        val jobAndJobItem = makeDownloadJobAndJobItem(contentEntry, container, downloadDir, clientDb)

        val downloadPlugin = ContainerDownloadPlugin(Any(), clientEndpoint, clientDi)
        val processResult: ProcessResult

        val downloadTime = measureTimeMillis {
            processResult = runBlocking {
                downloadPlugin.processJob(jobAndJobItem,
                    makeContentJobProcessContext(tempFolder, clientDb, clientDi), mockListener)
            }
        }

        println("Download completed in ${downloadTime}ms")

        Assert.assertEquals("Process Result reports success", JobStatus.COMPLETE,
            processResult.status)

        //Check it was downloaded correctly
        remoteDb.assertContainerEqualToOther(container.containerUid, clientDb)


    }


}