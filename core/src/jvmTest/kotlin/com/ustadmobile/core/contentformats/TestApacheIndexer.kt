package com.ustadmobile.core.contentformats

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.catalog.contenttype.*
import com.ustadmobile.core.contentjob.ContentPluginManager
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.DummyContentJobItemTransactionRunner
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.getSize
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.net.URL
import java.nio.file.Files


class TestApacheIndexer {

    @Rule
    @JvmField
    val tmpFileRule = TemporaryFolder()

    val tmpDir = Files.createTempDirectory("folder").toFile()
    val containerDir = Files.createTempDirectory("container").toFile()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    lateinit var db: UmAppDatabase

    lateinit var accountManager: UstadAccountManager

    lateinit var mockWebServer: MockWebServer

    private lateinit var di: DI
    private lateinit var endpointScope: EndpointScope


    @Before
    fun setup() {
        endpointScope = EndpointScope()

        di = DI {
            import(ustadTestRule.diModule)
            bind<EpubTypePluginCommonJvm>() with scoped(ustadTestRule.endpointScope).singleton {
                EpubTypePluginCommonJvm(Any(), context, di)
            }
            bind<XapiTypePluginCommonJvm>() with scoped(ustadTestRule.endpointScope).singleton {
                XapiTypePluginCommonJvm(Any(), context, di)
            }
            bind<H5PTypePluginCommonJvm>() with scoped(ustadTestRule.endpointScope).singleton {
                H5PTypePluginCommonJvm(Any(), context, di)
            }
            bind<VideoTypePluginJvm>() with scoped(ustadTestRule.endpointScope).singleton {
                VideoTypePluginJvm(Any(), context, di)
            }
            bind<PDFTypePluginJvm>() with scoped(ustadTestRule.endpointScope).singleton {
                PDFTypePluginJvm(Any(), context, di)
            }
            bind<ContentPluginManager>() with scoped(ustadTestRule.endpointScope).singleton {
                ContentPluginManager(listOf(
                        EpubTypePluginCommonJvm(Any(), context, di)
                    )
                )
            }
        }

        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = globalDisptacher

        accountManager = di.direct.instance()
        accountManager.activeEndpoint = Endpoint(mockWebServer.url("/").toString())


        db = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)




    }

    @Test
    fun givenApacheFolder_whenIndexed_createEntries() {

        val apacheIndexer = ApacheIndexerPlugin(Any(), accountManager.activeEndpoint, di)

        val jobAndItem = ContentJobItemAndContentJob()
        runBlocking {
            val job = ContentJob().apply {
                toUri = containerDir.toURI().toString()
                cjUid = db.contentJobDao.insertAsync(this)
            }
            val sourceURL = URL(mockWebServer.url("/json/com/ustadmobile/core/contenttype/folder.txt")
                    .toString())
            val item = ContentJobItem().apply {
                cjiJobUid = job.cjUid
                sourceUri = sourceURL.toURI().toString()
                cjiItemTotal = sourceUri?.let { DoorUri.parse(it).getSize(Any(), di)  } ?: 0L
                cjiPluginId = ApacheIndexerPlugin.PLUGIN_ID
                cjiContentEntryUid = 42
                cjiIsLeaf = false
                cjiParentContentEntryUid = 0
                cjiConnectivityNeeded = false
                cjiStatus = JobStatus.QUEUED
                cjiUid = db.contentJobItemDao.insertJobItem(this)
            }
            jobAndItem.contentJob = job
            jobAndItem.contentJobItem = item

            val processContext = ContentJobProcessContext(DoorUri(sourceURL.toURI()),
                tmpDir.toDoorUri(), mutableMapOf(), DummyContentJobItemTransactionRunner(db), di)
            apacheIndexer.processJob(jobAndItem, processContext){

            }


            val contentJobItems = db.contentJobItemDao.findAll().filter { it.cjiJobUid == job.cjUid }
            Assert.assertTrue("created more jobs",contentJobItems.size > 1)

        }
    }

}
