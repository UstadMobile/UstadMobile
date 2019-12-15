package com.ustadmobile.sharedse.network

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadRunner
import com.ustadmobile.lib.db.entities.DownloadJobItem
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import androidx.lifecycle.Observer
import androidx.room.Room
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.sharedse.network.ext.addTestChildDownload
import com.ustadmobile.sharedse.network.ext.addTestRootDownload
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.withTimeout
import org.junit.*
import org.junit.rules.TestRule


fun <T> LiveData<T>.deferredUntil(block: (T) -> Boolean): Deferred<T> {
    val completableDeferred = CompletableDeferred<T>()
    val observerFn = object: Observer<T> {
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

@RunWith(RobolectricTestRunner::class)
class ContainerDownloadManagerTest {

    private lateinit var context: Context

    //This rule is required to make LiveData postValue work
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()


    lateinit var clientDb: UmAppDatabase

    @Before
    fun setup() {
        context = RuntimeEnvironment.application.applicationContext
        clientDb = Room.databaseBuilder(context, UmAppDatabase::class.java, "UmAppDatabase")
                .allowMainThreadQueries()
                .build()
        clientDb.clearAllTables()
    }

    @After
    fun tearDown() {
        clientDb.close()
    }


    @Test
    fun givenParentDownloadJobCreated_whenChildItemIsAdded_thenSizeOfParentItemIsUpdated() {
        runBlocking {
            val downloadManagerImpl = DownloadManagerImpl(appDb = clientDb) {
                mock<ContainerDownloadRunner> {  }
            }

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
            val downloadManagerImpl = DownloadManagerImpl(appDb = clientDb) {
                mock<ContainerDownloadRunner> {  }
            }

            val (newDownloadJob, rootDownloadJobItem) = downloadManagerImpl.addTestRootDownload(1)
            val childDownloadJobItem = downloadManagerImpl.addTestChildDownload(rootDownloadJobItem,
                    DownloadJobItem(newDownloadJob, 2L, 2L, 1000L))

            val rootEntryLiveData = downloadManagerImpl.getDownloadJobItemByJobItemUid(rootDownloadJobItem.djiUid)
            val rootEntryDeferredUntilUpdate = rootEntryLiveData.deferredUntil { it?.downloadedSoFar == dlProgressAmount }

            val childUpdate = DownloadJobItem(childDownloadJobItem)
            childUpdate.downloadedSoFar = dlProgressAmount
            downloadManagerImpl.handleDownloadJobItemUpdated(childUpdate)

            val rootEntryUpdated = withTimeout(5000L * 1000L) { rootEntryDeferredUntilUpdate.await() }
            Assert.assertEquals("Root entry was updated with progress from child entry", dlProgressAmount,
                    rootEntryUpdated!!.downloadedSoFar)
        }
    }

    @Test
    fun givenAllOtherChildrenStatusCompleted_whenRemainingChildIsCompleted_thenParentStatusIsComplete() {
        runBlocking {
            val downloadManagerImpl = DownloadManagerImpl(appDb = clientDb) {
                mock<ContainerDownloadRunner> {  }
            }

            val (newDownloadJob, rootDownloadJobItem) = downloadManagerImpl.addTestRootDownload(1)
            val childDownloadJobItem1 = downloadManagerImpl.addTestChildDownload(rootDownloadJobItem,
                    DownloadJobItem(newDownloadJob, 2L, 2L, 1000L))
            val childDownloadJobItem2 = downloadManagerImpl.addTestChildDownload(rootDownloadJobItem,
                    DownloadJobItem(newDownloadJob, 3L, 3L, 1000L))

            val rootEntryLiveData = downloadManagerImpl.getDownloadJobItemByJobItemUid(rootDownloadJobItem.djiUid)
            val rootEntryDeferredUntilUpdate = rootEntryLiveData.deferredUntil { it?.djiStatus == JobStatus.COMPLETE }

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
        }
    }

    @Test
    fun givenContentEntryBeingObserved_whenRelatedDownloadJobIsCreatedAndUpdated_thenOnChangeIsCalled() {
        runBlocking {
            val dlProgressAmount = 650L
            val downloadManagerImpl = DownloadManagerImpl(appDb = clientDb) {
                mock<ContainerDownloadRunner> {  }
            }

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



}