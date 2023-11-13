package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.util.*
import com.ustadmobile.door.DoorUri
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
import java.io.IOException
import kotlin.test.Test
import com.ustadmobile.core.util.ext.encodeStringMapToString
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.util.test.initNapierLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import org.junit.BeforeClass
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import kotlin.jvm.Volatile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestContentImportJobRunner : AbstractMainDispatcherTest() {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var db: UmAppDatabase

    private lateinit var endpoint: Endpoint

    var numTimesToFail = 0

    var processJobCalled = false

    var jobCompleted = false

    private lateinit var json: Json

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    //The time that the dummy plugin will take to 'process' something
    @Volatile
    private var dummyPluginDelayTime = 100L

    inner class DummyPlugin(
        override val di: DI,
        endpoint: Endpoint,
        private val delayTime: Long = dummyPluginDelayTime,
    ) : ContentImporter{
        override val importerId: Int
            get() = TEST_PLUGIN_ID

        override val supportedFileExtensions: List<String>
            get() = listOf("txt")
        override val supportedMimeTypes: List<String>
            get() = listOf("text/plain")

        override val formatName: String
            get() = "Dummy Format"

        private val _processRequestedJobItems = MutableStateFlow<List<ContentJobItemAndContentJob>>(
            emptyList()
        )
        val processRequestedJobItems: Flow<List<ContentJobItemAndContentJob>> = _processRequestedJobItems.asStateFlow()

        val processCompletedJobItems = concurrentSafeListOf<ContentJobItemAndContentJob>()

        private val _cancelledJobItems = MutableStateFlow<List<Pair<ContentJobItemAndContentJob, CancellationException>>>(
            emptyList()
        )

        val canceledJobItems: Flow<List<Pair<ContentJobItemAndContentJob, CancellationException>>> = _cancelledJobItems.asStateFlow()

        override suspend fun extractMetadata(
            uri: DoorUri,
            originalFilename: String?,
        ): MetadataResult {
            return MetadataResult(ContentEntryWithLanguage().apply {
                title = uri.toString().substringAfterLast("/")
                description = "Desc"
            }, TEST_PLUGIN_ID)
        }

        override suspend fun processJob(
            jobItem: ContentJobItemAndContentJob,
            progressListener: ContentJobProgressListener,
            transactionRunner: ContentJobItemTransactionRunner,
        ): ProcessResult {
            return withContext(Dispatchers.Default) {
                processJobCalled = true
                _processRequestedJobItems.update { prev -> prev + listOf(jobItem) }

                println("processJobCalled")
                try {
                    println("start delay")

                    delay(delayTime)
                } catch (c: CancellationException) {
                    println("TestContentJobRunner: caught cancellation")
                    _cancelledJobItems.update { prev ->
                        prev + listOf(jobItem to c)
                    }

                    throw c
                }

                println("job completed")
                jobCompleted = true
                processCompletedJobItems += jobItem

                return@withContext ProcessResult(JobStatus.COMPLETE)
            }
        }
    }

    @Before
    fun setup() {
        processJobCalled = false
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

            bind<ContentImportersManager>() with scoped(ustadTestRule.endpointScope).singleton {
                mock{
                    on { getImporterById(any()) }.thenReturn(instance<DummyPlugin>())

                    on { requireImporterById(any()) }.thenAnswer {
                        DummyPlugin(di, context)
                    }

                    onBlocking { extractMetadata(any(), anyOrNull()) }.thenAnswer {
                        runBlocking {
                            instance<DummyPlugin>().extractMetadata(
                                it.getArgument(0) as DoorUri, null
                            )
                        }
                    }
                }
            }
        }
        json = di.direct.instance()

        val accountManager: UstadAccountManager = di.direct.instance()
        endpoint = accountManager.activeEndpoint
        db = di.directActiveDbInstance()
    }

    @Test(timeout = 15000)
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

        val runner = ContentImportJobRunner(2, endpoint, di, 5)
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
    }

    //TODO - This test tests **NOTHING**
    //@Test(timeout = 15000)
    fun givenJobCreated_whenJobItemFails_thenShouldRetry() {
        val pluginManager: ContentImportersManager by di.onActiveAccount().instance()
        numTimesToFail = 1
        val mockPlugin = mock<ContentImporter> {
            onBlocking { processJob(any(), any(), any()) }.thenAnswer {
                throw RuntimeException("Fail!")
            }
        }

        pluginManager.stub {
            on { getImporterById(any()) }.thenReturn(mockPlugin)
            on { requireImporterById(any()) }.thenReturn(mockPlugin)
        }
    }

    //HERE: when job item throws fatal exception - then will not retry

    @Test(timeout = 15000)
    fun givenJobCreated_whenJobItemFailsAndExceedsAllowableAttempts_thenShouldFail() {
        runBlocking {
            val maxAttempts = 3
            db.contentJobDao.insertAsync(ContentJob(cjUid = 2))
            val jobItems = listOf(
                ContentJobItem().apply {
                    cjiJobUid = 2
                    cjiConnectivityNeeded = false
                    cjiStatus = JobStatus.QUEUED
                    cjiPluginId = TEST_PLUGIN_ID
                    sourceUri = "dummy:///test_0"
                }
            )
            db.contentJobItemDao.insertJobItems(jobItems)
            val pluginManager: ContentImportersManager by di.onActiveAccount().instance()
            val mockPlugin = mock<ContentImporter> {
                onBlocking { processJob(any(), any(), any()) }.thenAnswer {
                    throw IOException("Fail!")
                }
            }

            pluginManager.stub {
                on { getImporterById(any()) }.thenReturn(mockPlugin)
                on { requireImporterById(any()) }.thenReturn(mockPlugin)
            }

            val runner = ContentImportJobRunner(2, endpoint, di, maxItemAttempts = maxAttempts)
            runner.runJob()


            val allJobItems = runBlocking { db.contentJobItemDao.findAll() }
            allJobItems.forEach {
                Assert.assertEquals("job attempted match count", maxAttempts, it.cjiAttemptCount)
                Assert.assertEquals("job failed", JobStatus.FAILED, it.cjiStatus)
                Assert.assertEquals("job failed", JobStatus.FAILED, it.cjiRecursiveStatus)
            }
        }
    }

    @Test(timeout = 15000)
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
            val pluginManager: ContentImportersManager by di.onActiveAccount().instance()
            pluginManager.stub {
                onBlocking { extractMetadata(any(), anyOrNull())}.thenAnswer {
                    throw IllegalStateException("unexpected error while extracting")
                }
            }

            val runner = ContentImportJobRunner(2, endpoint, di, maxItemAttempts = maxAttempts)
            runner.runJob()


            val allJobItems = runBlocking { db.contentJobItemDao.findAll() }
            allJobItems.forEach {
                Assert.assertEquals("job attempted match count", maxAttempts, it.cjiAttemptCount)
                Assert.assertEquals("job failed", JobStatus.FAILED, it.cjiRecursiveStatus)
            }
        }
    }

    @Test(timeout = 10000)
    fun givenJobCreated_whenJobCancelled_thenContentEntryShouldBeMadeInactive(){
        val contentJobId = 2L
        runBlocking {
            db.contentJobDao.insertAsync(ContentJob(cjUid = contentJobId))
            val contentJobItemUid = db.contentJobItemDao.insertJobItem(ContentJobItem().apply {
                cjiJobUid = contentJobId
                cjiConnectivityNeeded = false
                cjiStatus = JobStatus.QUEUED
                cjiPluginId = TEST_PLUGIN_ID
                sourceUri = "dummy:///test"
            })

            val mockPluginId = 42

            val dummyPlugin = DummyPlugin(di, endpoint, 100000)

            val mockPluginManager = mock<ContentImportersManager> {
                on { getImporterById(eq(mockPluginId)) }.thenReturn(dummyPlugin)

                on { requireImporterById(eq(mockPluginId)) }.thenAnswer {
                    dummyPlugin
                }

                onBlocking { extractMetadata(any(), anyOrNull()) }.thenAnswer {
                    runBlocking {
                        dummyPlugin.extractMetadata(it.getArgument(0), null)
                    }
                }
            }

            val jobRunnerDi = DI {
                extend(di)
                bind<ContentImportersManager>(overrides = true) with scoped(ustadTestRule.endpointScope).singleton {
                    mockPluginManager
                }
            }

            val runner = ContentImportJobRunner(2, endpoint, jobRunnerDi)

            val result = launch {
                runner.runJob()
            }

            //Wait for the Plugin's processJob to be called
            val processedItem = dummyPlugin.processRequestedJobItems
                .filter { it.isNotEmpty() }.first().first()

            result.cancelAndJoin()
            println("job cancelled")

            val canceledItem = dummyPlugin
                .canceledJobItems.first().first { it.first.contentJobItem?.cjiUid == contentJobItemUid }
            assertEquals(contentJobItemUid, canceledItem.first.contentJobItem?.cjiUid,
                message = "Content plugin received cancellation exception")

            val contentJobItemFromDb = db.contentJobItemDao.findRootJobItemByJobId(contentJobId)
            assertEquals(JobStatus.CANCELED, contentJobItemFromDb?.cjiStatus ?: -1,
                message = "Root ContentJobItem status is canceled")
            assertEquals(JobStatus.CANCELED, contentJobItemFromDb?.cjiRecursiveStatus ?: -1,
                message = "Root ContentJobItem recursive status is canceled")
            val contentEntryFromDb = db.contentEntryDao.findByUid(
                contentJobItemFromDb?.cjiContentEntryUid ?: 0)
            assertNotNull(contentEntryFromDb, "ContentEntry was created (e.g. after extracMetadata called)")
            assertTrue(contentEntryFromDb.ceInactive,
                "ContentEntry created by job was set as inactive when job cancelled")
        }
    }

    companion object {

        val TEST_PLUGIN_ID = 42

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            initNapierLog()
        }

    }

}