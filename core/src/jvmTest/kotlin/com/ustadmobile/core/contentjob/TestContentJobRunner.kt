package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveDbInstance
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import kotlin.test.Test
import org.junit.Before
import org.kodein.di.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.junit.Assert

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
        ): MetadataResult? {
            return null
        }

        override suspend fun processJob(
                jobItem: ContentJobItemAndContentJob,
                process: ProcessContext,
                progress: ContentJobProgressListener
        ): ProcessResult {
            delay(400)

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
                }
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
    }

    fun givenJobStartsWithoutAcceptableConnectivity_whenConnectivityAcceptable_thenShouldRunJobItems() {

    }

    fun givenJobCreated_whenJobItemFails_thenShouldRetry() {

    }

    fun givenJobCreated_whenJobItemFailsAndExceedsAllowableAttempts_thenShouldFail() {

    }


    companion object {
        val TEST_PLUGIN_ID = 42
    }

}