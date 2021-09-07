package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.ConnectivityLiveData
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveDbInstance
import com.ustadmobile.core.util.onActiveAccount
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import kotlin.test.Test
import org.junit.Before
import org.kodein.di.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.junit.Assert
import org.mockito.kotlin.stub

class TestContentJobRunner {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var db: UmAppDatabase

    private lateinit var endpoint: Endpoint

    class DummyPlugin(override val di: DI, endpoint: Endpoint) : ContentPlugin{
        override val pluginId: Int
            get() = TEST_PLUGIN_ID

        override val supportedFileExtensions: List<String>
            get() = listOf("txt")
        override val supportedMimeTypes: List<String>
            get() = listOf("text/plain")

        private val db: UmAppDatabase by on(endpoint).instance(tag = DoorTag.TAG_DB)

        override suspend fun extractMetadata(
            uri: DoorUri,
            process: ProcessContext
        ): MetadataResult {
            return MetadataResult(ContentEntryWithLanguage().apply {
                title = uri.toString().substringAfterLast("/")
                description = "Desc"
            }, TEST_PLUGIN_ID)
        }

        override suspend fun processJob(
                jobItem: ContentJobItemAndContentJob,
                process: ProcessContext,
                progress: ContentJobProgressListener
        ): ProcessResult {
            delay(100)

            db.contentJobItemDao.updateItemStatus(jobItem.contentJobItem?.cjiUid ?: 0,
                JobStatus.COMPLETE)
            return ProcessResult(200)
        }
    }

    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
            bind<ContentPluginManager>() with scoped(ustadTestRule.endpointScope).singleton {
                mock{
                    on { getPluginById(any()) }.thenAnswer {
                        DummyPlugin(di, context)
                    }

                    onBlocking { extractMetadata(any(), any()) }.thenAnswer {
                        runBlocking { DummyPlugin(di, context).extractMetadata(
                            it.getArgument(0) as DoorUri, mock {})
                        }
                    }
                }
            }

            bind<ConnectivityLiveData>() with scoped(ustadTestRule.endpointScope).singleton {
                val db : UmAppDatabase = on(context).instance(tag = DoorTag.TAG_DB)
                ConnectivityLiveData(db.connectivityStatusDao.statusLive())
            }
        }

        val accountManager: UstadAccountManager = di.direct.instance()
        endpoint = accountManager.activeEndpoint
        db = di.directActiveDbInstance()
    }

    @Test
    fun givenJobs_whenStarted_thenShouldRunThem() {
        val jobItems = (0 .. 100).map {
            ContentJobItem().apply {
                cjiJobUid = 2
                cjiConnectivityAcceptable = ContentJobItem.ACCEPT_ANY
                cjiStatus = JobStatus.QUEUED
                cjiPluginId = TEST_PLUGIN_ID
                sourceUri = "dummy:///test_$it"
            }
        }

        runBlocking {
            db.contentJobItemDao.insertJobItems(jobItems)
            db.contentJobDao.insertAsync(ContentJob(cjUid = 2L))
        }

        val runner = ContentJobRunner(2, endpoint, di, 5)
        runBlocking {
            runner.runJob()
        }

        val done = runBlocking {
            db.contentJobItemDao.isJobDone(2)
        }

        Assert.assertTrue("Job completed", done)
        val allJobItems = runBlocking { db.contentJobItemDao.findAll() }
        allJobItems.forEach {
            Assert.assertNotEquals("ContentJobItem contentEntryUid != 0 after completion",
                0L, it.cjiContentEntryUid)
            Assert.assertNotNull("ContentEntry created from extractMetadata",
                db.contentEntryDao.findByUid(it.cjiContentEntryUid))
        }
    }

    @Test
    fun givenJobStartsWithoutAcceptableConnectivity_whenConnectivityAcceptable_thenShouldRunJobItem() {
        runBlocking {
            db.contentJobDao.insertAsync(ContentJob(cjUid = 2))
            db.contentJobItemDao.insertJobItem(ContentJobItem().apply {
                this.cjiJobUid = 2
                cjiConnectivityAcceptable = ContentJobItem.ACCEPT_UNMETERED
                cjiStatus = JobStatus.QUEUED
                cjiPluginId = TEST_PLUGIN_ID
                sourceUri = "dummy:///test"
            })
            db.connectivityStatusDao.insert(
                ConnectivityStatus(ConnectivityStatus.STATE_METERED,
                    true, null))

            val runner = ContentJobRunner(2, endpoint, di)
            var doneBeforeConnectivityChange = true
            launch {
                delay(1000)
                doneBeforeConnectivityChange = db.contentJobItemDao.isJobDone(2)
                db.connectivityStatusDao.insert(
                    ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED,
                        true, null))
            }
            runner.runJob()

            val done = db.contentJobItemDao.isJobDone(2)
            Assert.assertTrue("Job completed now", done)
            Assert.assertFalse("Job was not done until connectivity status changed",
                doneBeforeConnectivityChange)
        }
    }

    @Test
    fun givenJobCreated_whenJobItemFails_thenShouldRetry() {

    }

    @Test
    fun givenJobCreated_whenJobItemFailsAndExceedsAllowableAttempts_thenShouldFail() {
        val pluginManager: ContentPluginManager by di.onActiveAccount().instance()
        val mockPlugin = mock<ContentPlugin> {
            onBlocking { processJob(any(), any(), any()) }.thenAnswer {
                throw RuntimeException("Fail!")
            }
        }

        pluginManager.stub {
            on { getPluginById(any()) }.thenReturn(mockPlugin)
        }

    }

    //TODO: test calling extract metadata when needed, getting plugin type when needed

    companion object {
        val TEST_PLUGIN_ID = 42
    }

}