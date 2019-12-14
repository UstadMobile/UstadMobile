package com.ustadmobile.sharedse.network

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadRunner
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.lib.db.entities.DownloadJobItemParentChildJoin
import com.ustadmobile.lib.db.entities.DownloadJobItemWithParents
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import androidx.lifecycle.Observer
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule


@RunWith(RobolectricTestRunner::class)
class ContainerDownloadManagerTest {

    private val context = RuntimeEnvironment.application.applicationContext

    //This rule is required to make LiveData postValue work
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()


    fun setup() {

    }

    @Test
    fun givenDownloadJobAndChildCreated_whenUpdatedToChildIsPosted_thenParentIsUpdated() {
        runBlocking {
            val clientDb = UmAppDatabase.getInstance(context)
            val downloadManagerImpl = DownloadManagerImpl(appDb = clientDb) {
                mock<ContainerDownloadRunner> {  }
            }
            val newDownloadJob = DownloadJob(1L, System.currentTimeMillis())

            downloadManagerImpl.createDownloadJob(newDownloadJob)
            val rootDownloadJobItem = DownloadJobItemWithParents(newDownloadJob, 1L, 0L, 0L,
                    mutableListOf())
            downloadManagerImpl.addItemsToDownloadJob(listOf(rootDownloadJobItem))

            val childDownloadJobItem = DownloadJobItemWithParents(newDownloadJob, 2L, 2L, 1000L,
                    mutableListOf(DownloadJobItemParentChildJoin(rootDownloadJobItem.djiUid, 0,1L)))
            downloadManagerImpl.addItemsToDownloadJob(listOf(childDownloadJobItem))

            val rootDownloadLiveData = downloadManagerImpl.getDownloadJobItemByJobItemUid(rootDownloadJobItem.djiUid)
            val completableDeferred = CompletableDeferred<DownloadJobItem?>()
            val observerFn = object: Observer<DownloadJobItem?> {
                override fun onChanged(t: DownloadJobItem?) {
                    if(t?.downloadLength == 1000L)
                        completableDeferred.complete(t)
                }
            }
            rootDownloadLiveData.observeForever(observerFn)

            Assert.assertEquals("Root job item size includes child item", 1000L,
                    completableDeferred.await()?.downloadLength)

            rootDownloadLiveData.removeObserver(observerFn)
        }
    }

    fun givenContentEntryLiveDataBeingObserved_whenDownloadJobIsCreated_thenOnChangeIsCalled() {

    }

    fun givenDownloadJobItemWithParent_whenAllChildItemsAreFinished_thenParentStatusIsChangedToFinished() {

    }




}