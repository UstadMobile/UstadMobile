package com.ustadmobile.core.contentformats

import org.mockito.kotlin.spy
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.catalog.contenttype.ApacheIndexerPlugin
import com.ustadmobile.core.catalog.contenttype.EpubTypePluginCommonJvm
import com.ustadmobile.core.contentjob.ContentPluginManager
import com.ustadmobile.core.contentjob.ContentPluginManagerImpl
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.contentjob.TestContentJobRunner
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.io.ext.getSize
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.clearAllTablesAndResetSync
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import java.io.File
import java.net.URL
import java.nio.file.Files
import javax.naming.InitialContext
import kotlin.random.Random


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
            bind<ContentPluginManager>() with scoped(ustadTestRule.endpointScope).singleton {
                ContentPluginManagerImpl(listOf(
                        EpubTypePluginCommonJvm(Any(), context, di)
                    )
                )
            }
        }

        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = globalDisptacher

        accountManager = di.direct.instance()
        accountManager.activeEndpoint = Endpoint(mockWebServer.url("/").toString())


        db = di.on(accountManager.activeEndpoint).direct.instance(tag = UmAppDatabase.TAG_DB)




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
            val item = ContentJobItem().apply {
                cjiJobUid = job.cjUid
                sourceUri = URL(mockWebServer.url("/json/com/ustadmobile/core/contenttype/folder.txt").toString()).toURI().toString()
                cjiItemTotal = sourceUri?.let { DoorUri.parse(it).getSize(Any(), di)  } ?: 0L
                cjiPluginId = ApacheIndexerPlugin.PLUGIN_ID
                cjiContentEntryUid = 42
                cjiIsLeaf = false
                cjiParentContentEntryUid = 0
                cjiConnectivityAcceptable = ContentJobItem.ACCEPT_ANY
                cjiStatus = JobStatus.QUEUED
                cjiUid = db.contentJobItemDao.insertJobItem(this)
            }
            jobAndItem.contentJob = job
            jobAndItem.contentJobItem = item

            apacheIndexer.processJob(jobAndItem, ProcessContext(tmpDir.toDoorUri(), mutableMapOf())){

            }


            val contentJobItems = db.contentJobItemDao.findAll().filter { it.cjiJobUid == job.cjUid }
            Assert.assertTrue("created more jobs",contentJobItems.size > 1)

        }
    }

}
