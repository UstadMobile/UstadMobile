package com.ustadmobile.sharedse.network

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadRunner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.sharedse.network.ext.addTestChildDownload
import com.ustadmobile.sharedse.network.ext.addTestRootDownload
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.kodein.di.*
import javax.naming.InitialContext


fun <T> DoorLiveData<T>.deferredUntil(block: (T) -> Boolean): Deferred<T> {
    val completableDeferred = CompletableDeferred<T>()
    val observerFn = object: DoorObserver<T> {
        override fun onChanged(t: T) {
            if(block(t))
                completableDeferred.complete(t)
        }
    }
    observeForever(observerFn)

    val deferred = completableDeferred
    deferred.invokeOnCompletion {
        removeObserver(observerFn)
    }
    return deferred
}

class ContainerDownloadManagerTest {


    lateinit var clientDb: UmAppDatabase

    lateinit var di: DI

    lateinit var mockDownloadRunner: ContainerDownloadRunner

    @Before
    fun setup() {
        mockDownloadRunner = mock<ContainerDownloadRunner> {

        }

        val endpointScope = EndpointScope()
        di = DI {
            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(endpointScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                spy(UmAppDatabase.getInstance(Any(), dbName).also {
                    it.clearAllTables()
                })
            }

            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_REPO) with scoped(endpointScope).singleton {
                spy(instance<UmAppDatabase>(tag = UmAppDatabase.TAG_DB).asRepository<UmAppDatabase>(Any(), context.url, "", defaultHttpClient(), null))
            }

            bind<ContainerDownloadRunner>() with factory {
                arg: DownloadJobItemRunnerDIArgs -> mockDownloadRunner
            }
        }

        clientDb = di.on(Endpoint(TEST_ENDPOINT)).direct.instance(tag = TAG_DB)
    }


    @Test
    fun givenParentDownloadJobCreated_whenChildItemIsAdded_thenSizeOfParentItemIsUpdated() {
        runBlocking {
            val downloadManagerImpl = ContainerDownloadManagerImpl(endpoint = Endpoint(TEST_ENDPOINT),
                di = di)

            val (newDownloadJob, rootDownloadJobItem) = downloadManagerImpl.addTestRootDownload()
            val childDownloadJobItem = downloadManagerImpl.addTestChildDownload(rootDownloadJobItem,
                    DownloadJobItem(newDownloadJob, 2L, 2L, 1000L))

            val rootDownloadLiveData = downloadManagerImpl.getDownloadJobItemByJobItemUid(rootDownloadJobItem.djiUid)
            val resizedRootItem = withTimeout(5000) {
                rootDownloadLiveData.deferredUntil { it?.downloadLength == 1000L }.await()
            }

            Assert.assertEquals("Root job item size includes child item", 1000L,
                    resizedRootItem!!.downloadLength)


            val rootEntryInDb = clientDb.downloadJobItemDao.findByUid(rootDownloadJobItem.djiUid)
            Assert.assertEquals("Size of root entry in database is 1000", 1000L,
                    rootEntryInDb!!.downloadLength)
        }
    }

    @Test
    fun givenParentAndChildJobCreated_whenChildItemIsUpdated_thenParentIsUpdated() {
        runBlocking {
            val dlProgressAmount = 650L
            val downloadManagerImpl = ContainerDownloadManagerImpl(endpoint = Endpoint(TEST_ENDPOINT),
                    di = di)

            val (newDownloadJob, rootDownloadJobItem) = downloadManagerImpl.addTestRootDownload(1)
            val childDownloadJobItem = downloadManagerImpl.addTestChildDownload(rootDownloadJobItem,
                    DownloadJobItem(newDownloadJob, 2L, 2L, 1000L))

            val rootEntryLiveData = downloadManagerImpl.getDownloadJobItemByJobItemUid(rootDownloadJobItem.djiUid)
            val rootEntryDeferredUntilUpdate = rootEntryLiveData.deferredUntil { it?.downloadedSoFar == dlProgressAmount }
            val downloadJobLiveData = downloadManagerImpl.getDownloadJob(rootDownloadJobItem.djiDjUid)
            val downloadJobDeferredUntilUpdate = downloadJobLiveData.deferredUntil { it?.bytesDownloadedSoFar == dlProgressAmount }

            val childUpdate = DownloadJobItem(childDownloadJobItem)
            childUpdate.downloadedSoFar = dlProgressAmount
            downloadManagerImpl.handleDownloadJobItemUpdated(childUpdate)

            val rootEntryUpdated = withTimeout(5000L) { rootEntryDeferredUntilUpdate.await() }
            Assert.assertEquals("Root entry was updated with progress from child entry", dlProgressAmount,
                    rootEntryUpdated!!.downloadedSoFar)
            val downloadJobUpdated = withTimeout(5000L) { downloadJobDeferredUntilUpdate.await() }
            Assert.assertEquals("Download Job itself was updated with progress from child entry", dlProgressAmount,
                    downloadJobUpdated!!.bytesDownloadedSoFar)
        }
    }

    @Test
    fun givenAllOtherChildrenStatusCompleted_whenRemainingChildIsCompleted_thenParentAndJobStatusIsComplete() {
        runBlocking {
            val downloadManagerImpl = ContainerDownloadManagerImpl(endpoint = Endpoint(TEST_ENDPOINT),
                    di = di)

            val (newDownloadJob, rootDownloadJobItem) = downloadManagerImpl.addTestRootDownload(1)
            val childDownloadJobItem1 = downloadManagerImpl.addTestChildDownload(rootDownloadJobItem,
                    DownloadJobItem(newDownloadJob, 2L, 2L, 1000L))
            val childDownloadJobItem2 = downloadManagerImpl.addTestChildDownload(rootDownloadJobItem,
                    DownloadJobItem(newDownloadJob, 3L, 3L, 1000L))

            val rootEntryLiveData = downloadManagerImpl.getDownloadJobItemByJobItemUid(rootDownloadJobItem.djiUid)
            val rootEntryDeferredUntilUpdate = rootEntryLiveData.deferredUntil { it?.djiStatus == JobStatus.COMPLETE }
            val downloadJobLiveData = downloadManagerImpl.getDownloadJob(newDownloadJob.djUid)
            val downloadJobDeferredUntilComplete = downloadJobLiveData.deferredUntil { it?.djStatus == JobStatus.COMPLETE }

            downloadManagerImpl.handleDownloadJobItemUpdated(DownloadJobItem(childDownloadJobItem1).also {
                it.djiStatus = JobStatus.COMPLETE
                it.downloadedSoFar = 1000L
            })

            downloadManagerImpl.handleDownloadJobItemUpdated(DownloadJobItem(childDownloadJobItem2).also {
                it.djiStatus = JobStatus.COMPLETE
                it.downloadedSoFar = 1000L
            })

            val completedRootEntry = withTimeout(5000L) { rootEntryDeferredUntilUpdate.await() }
            Assert.assertEquals("Parent status is set to complete when all children have completed",
                    JobStatus.COMPLETE, completedRootEntry?.djiStatus)

            val completedDownloadJob = withTimeout(5000L) { downloadJobDeferredUntilComplete.await() }
            Assert.assertEquals("Download job itself is set to completed when root entry is set to compelted",
                    JobStatus.COMPLETE, completedDownloadJob?.djStatus)
        }
    }

    @Test
    fun givenContentEntryBeingObserved_whenRelatedDownloadJobIsCreatedAndUpdated_thenOnChangeIsCalled() {
        runBlocking {
            val dlProgressAmount = 650L
            val downloadManagerImpl = ContainerDownloadManagerImpl(endpoint = Endpoint(TEST_ENDPOINT),
                    di = di)

            val rootEntryLiveData = downloadManagerImpl.getDownloadJobItemByContentEntryUid(1L)
            val deferredUntilRootProgressUpdated = rootEntryLiveData.deferredUntil { it?.downloadedSoFar == dlProgressAmount }

            val (newDownloadJob, rootDownloadJobItem) = downloadManagerImpl.addTestRootDownload(1)
            val childDownloadJobItem = downloadManagerImpl.addTestChildDownload(rootDownloadJobItem,
                    DownloadJobItem(newDownloadJob, 2L, 2L, 1000L))

            downloadManagerImpl.handleDownloadJobItemUpdated(DownloadJobItem(childDownloadJobItem).also {
                it.downloadedSoFar = dlProgressAmount
            })

            val rootEntryAfterUpdate = withTimeout(5000) { deferredUntilRootProgressUpdated.await() }
            Assert.assertEquals("Root entry was updated when obtained using jobItemByContentEntryUid",
                    dlProgressAmount, rootEntryAfterUpdate?.downloadedSoFar)
        }
    }

    @Test
    fun givenDownloadCreated_whenEnqueued_thenShouldInvokeRunner() {
        runBlocking {
            //val downloadFnCompletable = CompletableDeferred<Int>()
            val downloadManagerImpl = ContainerDownloadManagerImpl(endpoint = Endpoint(TEST_ENDPOINT),
                    di = di)
            downloadManagerImpl.handleConnectivityChanged(
                    ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED, true, "wifi"))

            val childLiveData = downloadManagerImpl.getDownloadJobItemByContentEntryUid(2L)
            val childLiveDataDeferred = childLiveData.deferredUntil { it?.djiStatus == JobStatus.QUEUED }
            val (newDownloadJob, rootDownloadJobItem) = downloadManagerImpl.addTestRootDownload(1)
            val childDownloadJobItem = downloadManagerImpl.addTestChildDownload(rootDownloadJobItem,
                    DownloadJobItem(newDownloadJob, 2L, 2L, 1000L))

            downloadManagerImpl.enqueue(newDownloadJob.djUid)

            verify(mockDownloadRunner, timeout(5000 * 5000)).download()

            val childLiveDataUpdated = withTimeout(5000) {childLiveDataDeferred.await() }
            Assert.assertEquals("Livedata update was sent that download job item was enqueued",
                    JobStatus.QUEUED, childLiveDataUpdated?.djiStatus)
        }
    }

    @Test
    fun givenDownloadRunning_whenDownloadCompletes_thenShouldInvokeRunnerForNextDownload() {
        runBlocking {
            val startedJobChannel = Channel<DownloadJobItem>(Channel.UNLIMITED)

            val diExtended = DI {
                extend(di)

                bind<ContainerDownloadRunner>(overrides = true) with factory {arg: DownloadJobItemRunnerDIArgs ->
                    mock<ContainerDownloadRunner> {
                        onBlocking { download() }.thenAnswer {
                            GlobalScope.launch {
                                startedJobChannel.send(arg.downloadJobItem)
                                delay(100)
                            }
                        }
                    }
                }
            }

            val downloadManagerImpl = ContainerDownloadManagerImpl(endpoint = Endpoint(TEST_ENDPOINT),
                    di = diExtended)

            downloadManagerImpl.handleConnectivityChanged(
                    ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED, true, "wifi"))

            val childDownloadJobs = (1..2).map {
                val (newDownloadJob, rootDownloadJobItem) = downloadManagerImpl.addTestRootDownload(it.toLong())
                downloadManagerImpl.addTestChildDownload(rootDownloadJobItem,
                        DownloadJobItem(newDownloadJob, it.toLong() + 10,
                                it.toLong() + 20, 1000L))
            }

            downloadManagerImpl.enqueue(childDownloadJobs[0].djiDjUid)
            downloadManagerImpl.enqueue(childDownloadJobs[1].djiDjUid)

            var numJobsStarted = 0
            for(downloadJobItem in startedJobChannel) {
                Assert.assertEquals("Download job item run matches download job item uid",
                        childDownloadJobs[numJobsStarted].djiUid, downloadJobItem.djiUid)

                downloadManagerImpl.handleDownloadJobItemUpdated(DownloadJobItem(downloadJobItem).also {
                    it.djiStatus = JobStatus.COMPLETE
                })

                numJobsStarted++
                if(numJobsStarted == childDownloadJobs.size)
                    break
            }
        }
    }

    @Test
    fun givenRunnerInvokedAndActive_whenDownloadPaused_thenShouldCallRunnerPause() {
        runBlocking {
            val downloadManagerImpl = ContainerDownloadManagerImpl(endpoint = Endpoint(TEST_ENDPOINT),
                    di = di)
            downloadManagerImpl.handleConnectivityChanged(
                    ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED, true, "wifi"))

            val (newDownloadJob, rootDownloadJobItem) = downloadManagerImpl.addTestRootDownload(1)
            val childDownloadJobItem = downloadManagerImpl.addTestChildDownload(rootDownloadJobItem,
                    DownloadJobItem(newDownloadJob, 2L, 2L, 1000L))

            downloadManagerImpl.enqueue(newDownloadJob.djUid)
            delay(100)
            downloadManagerImpl.pause(newDownloadJob.djUid)
            verifyBlocking(mockDownloadRunner, timeout(5000), { pause() })
        }
    }


    @Test
    fun givenRunnerInvokedAndActive_whenDownloadCancelled_thenShouldCallRunnerCancel() {
        runBlocking {
            val downloadManagerImpl = ContainerDownloadManagerImpl(endpoint = Endpoint(TEST_ENDPOINT),
                    di = di)
            downloadManagerImpl.handleConnectivityChanged(
                    ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED, true, "wifi"))

            val (newDownloadJob, rootDownloadJobItem) = downloadManagerImpl.addTestRootDownload(1)
            val childDownloadJobItem = downloadManagerImpl.addTestChildDownload(rootDownloadJobItem,
                    DownloadJobItem(newDownloadJob, 2L, 2L, 1000L))

            downloadManagerImpl.enqueue(newDownloadJob.djUid)
            verify(mockDownloadRunner, timeout(5000)).download()
            delay(100)
            downloadManagerImpl.cancel(newDownloadJob.djUid)

            verifyBlocking(mockDownloadRunner, timeout(5000), { cancel() })
        }
    }

    @Test
    fun givenRunnerInvokedAndActive_whenSetMeteredDataAllowedCalled_thenShouldCallSetMeteredDataAllowed() {
        runBlocking {
            val downloadManagerImpl = ContainerDownloadManagerImpl(endpoint = Endpoint(TEST_ENDPOINT),
                    di = di)

            downloadManagerImpl.handleConnectivityChanged(
                    ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED, true, "wifi"))

            val (newDownloadJob, rootDownloadJobItem) = downloadManagerImpl.addTestRootDownload(1)
            val childDownloadJobItem = downloadManagerImpl.addTestChildDownload(rootDownloadJobItem,
                    DownloadJobItem(newDownloadJob, 2L, 2L, 1000L))

            downloadManagerImpl.enqueue(newDownloadJob.djUid)
            delay(100)
            downloadManagerImpl.setMeteredDataAllowed(newDownloadJob.djUid, false)
            verify(mockDownloadRunner, timeout(5000)).meteredDataAllowed = false
        }

    }

    @Test
    fun givenDownloadEnqueuedWithNoUnmeteredConnectivity_whenConnectivityChanges_thenShouldInvokeRunner() {
        runBlocking {
            val downloadManagerImpl = ContainerDownloadManagerImpl(endpoint = Endpoint(TEST_ENDPOINT),
                    di = di)

            val (newDownloadJob, rootDownloadJobItem) = downloadManagerImpl.addTestRootDownload(1)
            val childDownloadJobItem = downloadManagerImpl.addTestChildDownload(rootDownloadJobItem,
                    DownloadJobItem(newDownloadJob, 2L, 2L, 1000L))
            downloadManagerImpl.enqueue(newDownloadJob.djUid)

            //download should not start when unmetered connectivity is not available
            verify(mockDownloadRunner, timeout(1000).times(0)).download()
            downloadManagerImpl.handleConnectivityChanged(
                    ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED, true, "wifi"))
            verifyBlocking(mockDownloadRunner, timeout(1000), { download() })
        }
    }



    companion object {

        const val TEST_ENDPOINT = "http://test.localhost.com/"

    }
}