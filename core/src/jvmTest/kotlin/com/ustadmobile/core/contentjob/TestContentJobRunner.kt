package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.core.networkmanager.ConnectivityLiveData
import com.ustadmobile.core.util.*
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import java.io.File
import java.io.IOException
import kotlin.test.Test
import com.ustadmobile.core.util.ext.encodeStringMapToString
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.util.test.ext.baseDebugIfNotEnabled
import io.github.aakira.napier.Napier
import org.junit.BeforeClass
import kotlin.jvm.Volatile

class TestContentJobRunner {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var endpoint: Endpoint

    var numTimesToFail = 0

    var processJobCalled = false
    var jobCompleted = false
    var connectivityCancelledExceptionCalled = false
    var cancellationExceptionCalled = false

    private lateinit var json: Json

    private var jobContentParmas: Map<String, String>? = null

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    //The time that the dummy plugin will take to 'process' something
    @Volatile
    private var dummyPluginDelayTime = 100L

    inner class DummyPlugin(
        override val di: DI,
        endpoint: Endpoint
    ) : ContentPlugin{
        override val pluginId: Int
            get() = TEST_PLUGIN_ID

        override val supportedFileExtensions: List<String>
            get() = listOf("txt")
        override val supportedMimeTypes: List<String>
            get() = listOf("text/plain")

        private val db: UmAppDatabase by on(endpoint).instance(tag = DoorTag.TAG_DB)

        private val containerStorageManager: ContainerStorageManager by on(endpoint).instance()

        override suspend fun extractMetadata(
            uri: DoorUri,
            process: ContentJobProcessContext
        ): MetadataResult {
            return MetadataResult(ContentEntryWithLanguage().apply {
                title = uri.toString().substringAfterLast("/")
                description = "Desc"
            }, TEST_PLUGIN_ID)
        }

        override suspend fun processJob(
            jobItem: ContentJobItemAndContentJob,
            process: ContentJobProcessContext,
            progress: ContentJobProgressListener
        ): ProcessResult {
            jobContentParmas = process.params
            return withContext(Dispatchers.Default) {
                processJobCalled = true
                println("processJobCalled")
                try {
                    println("start delay")
                    repo.addEntriesToContainerFromZipResource(
                            jobItem.contentJobItem!!.cjiContainerUid, this::class.java,
                            "/com/ustadmobile/core/contentformats/epub/test.epub",
                            ContainerAddOptions(
                                DoorUri.parse(containerStorageManager.storageList.first().dirUri)))

                    delay(dummyPluginDelayTime)
                } catch (c: CancellationException) {
                    println("TestContentJobRunner: caught cancellation")
                    withContext(NonCancellable) {
                        if (c is ConnectivityCancellationException) {
                            connectivityCancelledExceptionCalled = true
                        } else {
                            cancellationExceptionCalled = true
                        }
                    }
                    throw c
                }

                println("job completed")
                jobCompleted = true

                return@withContext ProcessResult(JobStatus.COMPLETE)
            }
        }
    }

    @Before
    fun setup() {
        processJobCalled = false
        connectivityCancelledExceptionCalled = false
        cancellationExceptionCalled = false
        jobContentParmas = null
        jobCompleted = false
        dummyPluginDelayTime = 100L
        di = DI {
            import(ustadTestRule.diModule)

            bind<ContainerStorageManager>() with scoped(ustadTestRule.endpointScope).singleton {
                ContainerStorageManager(listOf(temporaryFolder.newFolder()))
            }

            bind<DummyPlugin>() with scoped(ustadTestRule.endpointScope).singleton {
                DummyPlugin(di, context)
            }

            bind<ContentPluginManager>() with scoped(ustadTestRule.endpointScope).singleton {
                mock{
                    on { getPluginById(any()) }.thenReturn(instance<DummyPlugin>())

                    on { requirePluginById(any()) }.thenAnswer {
                        DummyPlugin(di, context)
                    }

                    onBlocking { extractMetadata(any(), any()) }.thenAnswer {
                        runBlocking { instance<DummyPlugin>().extractMetadata(
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
        json = di.direct.instance()

        val accountManager: UstadAccountManager = di.direct.instance()
        endpoint = accountManager.activeEndpoint
        db = di.directActiveDbInstance()
        repo = di.directActiveRepoInstance()
    }

    @Test
    fun givenJobs_whenStarted_thenShouldRunThem() {
        val jobItems = (0 .. 20).map {
            ContentJobItem().apply {
                cjiJobUid = 2
                cjiConnectivityNeeded = false
                cjiStatus = JobStatus.QUEUED
                cjiPluginId = TEST_PLUGIN_ID
                sourceUri = "dummy:///test_$it"
            }
        }

        runBlocking {
            db.contentJobItemDao.insertJobItems(jobItems)
            db.contentJobDao.insertAsync(ContentJob(cjUid = 2L,
                params = json.encodeStringMapToString(mapOf("compress" to "true"))))
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
            Assert.assertEquals("jobStatus complete", JobStatus.COMPLETE, it.cjiStatus)
        }

        Assert.assertEquals("Content job params were provided as inserted on db",
            mapOf("compress" to "true"), jobContentParmas)
    }

    @Test
    fun givenJobStartsWithoutAcceptableConnectivity_whenConnectivityAcceptable_thenShouldRunJobItem() {
        runBlocking {
            db.contentJobDao.insertAsync(ContentJob(cjUid = 2))
            db.contentJobItemDao.insertJobItem(ContentJobItem().apply {
                this.cjiJobUid = 2
                cjiConnectivityNeeded = true
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
                        true,
            null))
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
        val pluginManager: ContentPluginManager by di.onActiveAccount().instance()
        numTimesToFail = 1
        val mockPlugin = mock<ContentPlugin> {
            onBlocking { processJob(any(), any(), any()) }.thenAnswer {
                throw RuntimeException("Fail!")
            }
        }
        pluginManager.stub {
            on { getPluginById(any()) }.thenReturn(mockPlugin)
            on { requirePluginById(any()) }.thenReturn(mockPlugin)
        }


    }

    @Test
    fun givenJobCreated_whenJobItemFailsAndExceedsAllowableAttempts_thenShouldFail() {
        runBlocking {
            val maxAttempts = 3
            db.contentJobDao.insertAsync(ContentJob(cjUid = 2))
            val jobItems = listOf(ContentJobItem().apply {
                cjiJobUid = 2
                cjiConnectivityNeeded = false
                cjiStatus = JobStatus.QUEUED
                cjiPluginId = TEST_PLUGIN_ID
                sourceUri = "dummy:///test_0"
            })
            db.contentJobItemDao.insertJobItems(jobItems)
            val pluginManager: ContentPluginManager by di.onActiveAccount().instance()
            val mockPlugin = mock<ContentPlugin> {
                onBlocking { processJob(any(), any(), any()) }.thenAnswer {
                    throw IOException("Fail!")
                }
            }

            pluginManager.stub {
                on { getPluginById(any()) }.thenReturn(mockPlugin)
                on { requirePluginById(any()) }.thenReturn(mockPlugin)
            }

            val runner = ContentJobRunner(2, endpoint, di, maxItemAttempts = maxAttempts)
            runner.runJob()


            val allJobItems = runBlocking { db.contentJobItemDao.findAll() }
            allJobItems.forEach {
                Assert.assertEquals("job attempted match count", maxAttempts, it.cjiAttemptCount)
                Assert.assertEquals("job failed", JobStatus.FAILED, it.cjiRecursiveStatus)
            }
        }
    }

    @Test
    fun givenJobCreated_whenJobItemFailsWhenExtractMetadataAndExceedsAllowableAttempts_thenShouldFail() {
        runBlocking {
            val maxAttempts = 3
            db.contentJobDao.insertAsync(ContentJob(cjUid = 2))
            val jobItems = (0 .. 2).map {
                ContentJobItem().apply {
                    cjiJobUid = 2
                    cjiConnectivityNeeded = false
                    cjiStatus = JobStatus.QUEUED
                    cjiPluginId = 0
                    sourceUri = "dummy:///test_$it"
                }
            }
            db.contentJobItemDao.insertJobItems(jobItems)
            val pluginManager: ContentPluginManager by di.onActiveAccount().instance()
            pluginManager.stub {
                onBlocking { extractMetadata(any(), any())}.thenAnswer {
                    throw IllegalStateException("unexpected error while extracting")
                }
            }

            val runner = ContentJobRunner(2, endpoint, di, maxItemAttempts = maxAttempts)
            runner.runJob()


            val allJobItems = runBlocking { db.contentJobItemDao.findAll() }
            allJobItems.forEach {
                Assert.assertEquals("job attempted match count", maxAttempts, it.cjiAttemptCount)
                Assert.assertEquals("job failed", JobStatus.FAILED, it.cjiRecursiveStatus)
            }
        }
    }

    @Test
    fun givenJobCreated_whenJobItemNotSupportedWhenExtractMetadata_thenJobItemCompleted() {
        runBlocking {
            val maxAttempts = 3
            db.contentJobDao.insertAsync(ContentJob(cjUid = 2))
            val jobItems = (0 .. 2).map {
                ContentJobItem().apply {
                    cjiJobUid = 2
                    cjiConnectivityNeeded = false
                    cjiStatus = JobStatus.QUEUED
                    sourceUri = "dummy:///test_$it"
                }
            }
            db.contentJobItemDao.insertJobItems(jobItems)
            val pluginManager: ContentPluginManager by di.onActiveAccount().instance()
            pluginManager.stub {
                onBlocking { extractMetadata(any(), any())}.thenAnswer {
                    throw ContentTypeNotSupportedException()
                }
            }

            val runner = ContentJobRunner(2, endpoint, di, maxItemAttempts = maxAttempts)
            runner.runJob()


            val allJobItems = runBlocking { db.contentJobItemDao.findAll() }
            allJobItems.forEach {
                Assert.assertEquals("job completed", JobStatus.COMPLETE, it.cjiRecursiveStatus)
            }
        }
    }


    //Test temporarily disabled 12/Sept/22- this is flaky, and will be replaced by the attachment system.
    //@Test
    fun givenJobCreated_whenJobConnectivityChangesToUnAcceptable_thenJobCancelledAndQueued(){
        dummyPluginDelayTime = 10000
        runBlocking {
            db.contentJobDao.insertAsync(ContentJob(cjUid = 2))
            db.contentJobItemDao.insertJobItem(ContentJobItem().apply {
                this.cjiJobUid = 2
                cjiConnectivityNeeded = true
                cjiStatus = JobStatus.QUEUED
                cjiPluginId = TEST_PLUGIN_ID
                sourceUri = "dummy:///test"
            })
            db.connectivityStatusDao.insert(
                    ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED,
                            true, null))


            val runner = ContentJobRunner(2, endpoint, di)
            launch {
                delay(1000)
                db.connectivityStatusDao.insert(
                        ConnectivityStatus(ConnectivityStatus.STATE_METERED,
                                true, null))
            }
            val result = launch {
                runner.runJob()
            }

            delay(5000)

            val job = db.contentJobItemDao.findRootJobItemByJobId(2)!!
            Assert.assertTrue("connectivity exception called from plugin", connectivityCancelledExceptionCalled)
            Assert.assertFalse("job not completed", jobCompleted)
            Assert.assertTrue("content Entry got created from extract metadata", job.cjiContentEntryUid != 0L)
            Assert.assertTrue("job finished 1st time but interrupted", job.cjiFinishTime != 0L)
            Assert.assertEquals("Job back to queued", JobStatus.QUEUED, job.cjiRecursiveStatus)
            // need to cancel, job waiting for connectivity to turn back on
            result.cancel()
        }
    }

    @Test
    fun givenJobCreated_whenJobCancelled_thenContentEntryShouldBeInvalidAndContainerDeleted(){
        val contentJobId = 2L
        runBlocking {
            ContentEntry().apply {
                contentEntryUid = 3
                repo.contentEntryDao.insert(this)
            }
            Container().apply {
                containerUid = 3
                containerContentEntryUid = 3
                repo.containerDao.insert(this)
            }
            db.contentJobDao.insertAsync(ContentJob(cjUid = contentJobId))
            db.contentJobItemDao.insertJobItem(ContentJobItem().apply {
                this.cjiJobUid = 2
                cjiContentEntryUid = 3
                cjiContainerUid = 3
                cjiConnectivityNeeded = false
                cjiStatus = JobStatus.QUEUED
                cjiPluginId = TEST_PLUGIN_ID
                cjiContentDeletedOnCancellation = true
                sourceUri = "dummy:///test"
            })

            val runner = ContentJobRunner(2, endpoint, di)

            val result = launch {
                runner.runJob()
            }


            do{
                delay(10)
            }while(!processJobCalled)


            result.cancelAndJoin()
            println("job cancelled")


            Assert.assertTrue("cancellation exception called from plugin",
                cancellationExceptionCalled)
            Assert.assertFalse("job not completed", jobCompleted)
            val contentJobItemFromDb = db.contentJobItemDao.findRootJobItemByJobId(contentJobId)

            Assert.assertEquals("Root ContentJobItem status is canceled", JobStatus.CANCELED,
                contentJobItemFromDb?.cjiStatus ?: -1)

            Assert.assertEquals("Root ContentJobItem recursive status is canceled",
                JobStatus.CANCELED,contentJobItemFromDb?.cjiRecursiveStatus ?: -1)

            val containerStorageManager: ContainerStorageManager = di.onActiveAccountDirect()
                .instance()
            val containerFolder = DoorUri.parse(containerStorageManager.storageList.first().dirUri)
                .toFile()
            val allJobItems = runBlocking { db.contentJobItemDao.findAll() }
            allJobItems.forEach {
                Assert.assertEquals("job is cancelled", JobStatus.CANCELED,
                    it.cjiStatus)
                val entry = db.contentEntryDao.findByUid(it.cjiContentEntryUid)
                Assert.assertEquals("entry is inActive", true, entry!!.ceInactive)
                val listOfEntryAndFile = db.containerEntryDao.findByContainer(it.cjiContainerUid)
                Assert.assertEquals("no files and containerEntry remain", 0,
                    listOfEntryAndFile.size)
                Assert.assertTrue("container folder doesnt exist", !File(containerFolder,
                    "${it.cjiContainerUid}").exists())
                Assert.assertEquals("ContentJobItem recursive status is canceled",
                    JobStatus.CANCELED, it.cjiRecursiveStatus)
                Assert.assertEquals("ContentJobItem status is canceled",
                    JobStatus.CANCELED, it.cjiStatus)
            }

        }

    }



    //TODO: test calling extract metadata when needed, getting plugin type when needed

    companion object {
        val TEST_PLUGIN_ID = 42

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            Napier.baseDebugIfNotEnabled()
        }

    }

}