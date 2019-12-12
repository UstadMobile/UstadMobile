package com.ustadmobile.door

import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.door.DoorDatabaseRepository.Companion.STATUS_CONNECTED
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_FAILED_NOCONNECTIVITYORPEERS
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADED_WITHDATA
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADING_CLOUD
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADING_MIRROR
import kotlinx.coroutines.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger


class RepositoryLoadHelperTest  {

    data class DummyEntity(val uid: Long, val firstName: String, val lastName: String)

    @Before
    fun setup() {
        Napier.base(DebugAntilog())
    }

    @Test
    fun givenLoadSuccessful_whenDoRequestCalledAgain_thenShouldNotLoadAgain() {
        val mockRepository = mock<DoorDatabaseRepository> {
            on {connectivityStatus}.thenReturn(DoorDatabaseRepository.STATUS_CONNECTED)
        }

        val invocationCount = AtomicInteger()
        val repoLoadHelper = RepositoryLoadHelper<DummyEntity>(mockRepository,
                lifecycleHelperFactory = mock {  }) {endpoint ->
            invocationCount.incrementAndGet()
            val entity = DummyEntity(100, "Bob", "Jones$endpoint")
            entity
        }


        val loadCallback = mock<RepositoryLoadHelper.RepoLoadCallback>()
        repoLoadHelper.addRepoLoadCallback(loadCallback)
        runBlocking {
            argumentCaptor<Int>().apply {
                repoLoadHelper.doRequest()
                repoLoadHelper.doRequest()

                Assert.assertEquals("RepoLoadHelper only calls the actual request once if the " +
                        "request was successful" ,
                        1, invocationCount.get())

                verify(loadCallback, times(3)).onLoadStatusChanged(capture(), anyOrNull())
                Assert.assertEquals("First status value is 0 (didnt start)", 0,
                        firstValue)
                Assert.assertEquals("Second status is LOADING_FROM_CLOUD",
                        STATUS_LOADING_CLOUD, allValues[1])
                Assert.assertEquals("Third final value is LOADED_WITH_DATA",
                        STATUS_LOADED_WITHDATA, lastValue)
            }
        }
    }

    @Test
    fun givenLoadUnsuccessfulWithNoConnectivityAndIsNotObserved_whenConnectivityResumed_thenShouldNotLoadAgain() {
        val currentConnectivityStatus = AtomicInteger(DoorDatabaseRepository.STATUS_DISCONNECTED)
        val mockRepository = mock<DoorDatabaseRepository> {
            on {connectivityStatus}.thenReturn(currentConnectivityStatus.get())
            onBlocking { activeMirrors() }.thenReturn(listOf())
        }

        val entity = DummyEntity(100, "Bob", "Jones")
        val completableDeferred = CompletableDeferred<DummyEntity>()

        val repoLoadHelper = RepositoryLoadHelper<DummyEntity>(mockRepository,
                lifecycleHelperFactory = mock {  }) {endpoint ->
            if(currentConnectivityStatus.get() == DoorDatabaseRepository.STATUS_CONNECTED) {
                completableDeferred.complete(entity)
                entity
            }else {
                throw IOException("Mock IOException Not connected")
            }
        }
        val loadCallback = mock<RepositoryLoadHelper.RepoLoadCallback>()
        repoLoadHelper.addRepoLoadCallback(loadCallback)

        val mockLiveData = mock<DoorLiveData<DummyEntity>> {  }
        val wrappedLiveData = repoLoadHelper.wrapLiveData(mockLiveData)

        runBlocking {
            try {
                repoLoadHelper.doRequest()
            }catch(e: Exception) {

            }

            currentConnectivityStatus.set(DoorDatabaseRepository.STATUS_CONNECTED)
            repoLoadHelper.onConnectivityStatusChanged(currentConnectivityStatus.get())
            val entityWithTimeout = withTimeoutOrNull(2000) { completableDeferred.await() }
            Assert.assertNull("When data is not being observed then a repeat request will " +
                    "not be made even when connectivity comes back",
                    entityWithTimeout)

            completableDeferred.cancel()

            argumentCaptor<Int>().apply {
                verify(loadCallback, times(3)).onLoadStatusChanged(capture(), anyOrNull())
                Assert.assertEquals("Last status callback was failed with no connectivity or peers",
                        STATUS_FAILED_NOCONNECTIVITYORPEERS, lastValue)
            }

        }
    }

    @Test
    fun givenLoadUnsuccessfulWithNoConnectivityAndIsObserved_whenConnectivityResumed_thenShouldLoadAgain() {
        val currentConnectivityStatus = AtomicInteger(DoorDatabaseRepository.STATUS_DISCONNECTED)
        val mockRepository = mock<DoorDatabaseRepository> {
            on {connectivityStatus}.thenAnswer { invocation -> currentConnectivityStatus.get() }
        }

        val entity = DummyEntity(100, "Bob", "Jones")
        val completableDeferred = CompletableDeferred<DummyEntity>()

        val repoLoadHelper = RepositoryLoadHelper<DummyEntity>(mockRepository,
                lifecycleHelperFactory = mock {  }) {endpoint ->
            if(currentConnectivityStatus.get() == DoorDatabaseRepository.STATUS_CONNECTED) {
                completableDeferred.complete(entity)
                entity
            }else {
                throw IOException("Mock IOException Not connected")
            }
        }
        val repoLoadCallback = mock<RepositoryLoadHelper.RepoLoadCallback>()
        repoLoadHelper.addRepoLoadCallback(repoLoadCallback)

        val mockLiveData = mock<DoorLiveData<DummyEntity>> {  }
        val wrappedLiveData = repoLoadHelper.wrapLiveData(mockLiveData)
                as RepositoryLoadHelper<DummyEntity>.LiveDataWrapper<DummyEntity>

        runBlocking {
            try {
                //mark that there is an active observer - this will fail because it's still disconnected
                wrappedLiveData.addActiveObserver(mock {  })
            } catch(e: Exception) {
                println(e)
                //do nothing
            }

            try {
                //run the request itself the same as the repository itself wouldo
                repoLoadHelper.doRequest()
            } catch (e: Exception) {

            }

            currentConnectivityStatus.set(DoorDatabaseRepository.STATUS_CONNECTED)
            repoLoadHelper.onConnectivityStatusChanged(DoorDatabaseRepository.STATUS_CONNECTED)

            val entityWithTimeout = withTimeout(5000 ) { completableDeferred.await() }
            Assert.assertEquals("After connectivity is restored and the obserer is active, " +
                    "the loadhelper automatically calls the request function", entity,
                    entityWithTimeout)
            verify(repoLoadCallback, timeout(5000))
                    .onLoadStatusChanged(eq(STATUS_LOADED_WITHDATA), anyOrNull())
            argumentCaptor<Int>() {
                verify(repoLoadCallback, atLeast(2)).onLoadStatusChanged(capture(), anyOrNull())
                Assert.assertTrue("Status failed to load due to no connectivity was shown",
                        allValues.any { it == STATUS_FAILED_NOCONNECTIVITYORPEERS})
                Assert.assertEquals("Last value was loaded successfully", STATUS_LOADED_WITHDATA,
                        lastValue)
            }
        }
    }


    @Test
    fun givenLoadUnsuccessful_whenObservedAgainAndConnectivityAvailable_thenShouldLoadAgain() {
        val currentConnectivityStatus = AtomicInteger(DoorDatabaseRepository.STATUS_DISCONNECTED)
        val mockRepository = mock<DoorDatabaseRepository> {
            on {connectivityStatus}.thenAnswer { invocation -> currentConnectivityStatus.get() }
            onBlocking { activeMirrors() }.thenReturn(listOf())
        }

        val entity = DummyEntity(100, "Bob", "Jones")
        val completableDeferred = CompletableDeferred<DummyEntity>()

        val loadHelperCallCount = AtomicInteger()
        val repoLoadHelper = RepositoryLoadHelper<DummyEntity>(mockRepository,
                lifecycleHelperFactory = mock {  }) {endpoint ->
            loadHelperCallCount.incrementAndGet()
            if(currentConnectivityStatus.get() == DoorDatabaseRepository.STATUS_CONNECTED) {
                completableDeferred.complete(entity)
                entity
            }else {
                throw IOException("Mock IOException Not connected")
            }
        }
        val repoLoadCallback = mock<RepositoryLoadHelper.RepoLoadCallback>()
        repoLoadHelper.addRepoLoadCallback(repoLoadCallback)

        val mockLiveData = mock<DoorLiveData<DummyEntity>> {  }
        val wrappedLiveData = repoLoadHelper.wrapLiveData(mockLiveData)
                as RepositoryLoadHelper<DummyEntity>.LiveDataWrapper<DummyEntity>

        runBlocking {
            try {
                repoLoadHelper.doRequest()
            }catch(e: Exception) {
                //do nothing - this will fail as connectivity is off
            }

            val callCountBeforeConnectivityRestored = loadHelperCallCount.get()
            currentConnectivityStatus.set(DoorDatabaseRepository.STATUS_CONNECTED)
            repoLoadHelper.onConnectivityStatusChanged(DoorDatabaseRepository.STATUS_CONNECTED)
            delay(2000)
            val callCountAfterConnectivityRestored = loadHelperCallCount.get()

            //now observe it - this should trigger a call to try the request again
            wrappedLiveData.addActiveObserver(mock {})
            val entityResult = withTimeout(2000) { completableDeferred.await() }


            Assert.assertEquals("When restoring connectivity with no active obserers there were" +
                    "no calls to the load function", callCountBeforeConnectivityRestored,
                    callCountAfterConnectivityRestored)

            Assert.assertEquals("When an observer is added after connectivity comes back, the request" +
                    "is automatically retried", entity, entityResult)

            verify(repoLoadCallback, timeout(5000))
                    .onLoadStatusChanged(eq(STATUS_LOADED_WITHDATA), anyOrNull())
            argumentCaptor<Int>() {
                verify(repoLoadCallback, atLeastOnce()).onLoadStatusChanged(capture(), anyOrNull())
                Assert.assertEquals("Last status value is loaded with data", STATUS_LOADED_WITHDATA,
                        lastValue)
                Assert.assertTrue("Status was set to FAILED due to no connection",
                        allValues.any { it == STATUS_FAILED_NOCONNECTIVITYORPEERS})
            }
        }
    }

    @Test
    fun givenLoadUnsucessful_whenObservedAgainAndNoConnectivityAvailable_thenShouldNotLoadAgain() {
        val mockRepository = mock<DoorDatabaseRepository> {
            on {connectivityStatus}.thenReturn(DoorDatabaseRepository.STATUS_DISCONNECTED)
            onBlocking { activeMirrors() }.thenReturn(listOf())
        }


        val loadHelperCallCount = AtomicInteger()
        val repoLoadHelper = RepositoryLoadHelper<DummyEntity>(mockRepository,
                lifecycleHelperFactory = mock {  }) {endpoint ->
            loadHelperCallCount.incrementAndGet()
            throw IOException("Mock IOException Not connected")
        }
        val loadCallback = mock<RepositoryLoadHelper.RepoLoadCallback>()
        repoLoadHelper.addRepoLoadCallback(loadCallback)

        val mockLiveData = mock<DoorLiveData<DummyEntity>> {  }
        val wrappedLiveData = repoLoadHelper.wrapLiveData(mockLiveData)
                as RepositoryLoadHelper<DummyEntity>.LiveDataWrapper<DummyEntity>

        runBlocking {
            try {
                repoLoadHelper.doRequest()
            } catch (e: Exception) {
                //do nothing - this will fail as connectivity is off
            }

            val callCountBeforeObserving = loadHelperCallCount.get()
            wrappedLiveData.addActiveObserver(mock {})
            delay(2000)

            Assert.assertEquals("When adding an observer there are no further calls to the load" +
                    "function", callCountBeforeObserving, loadHelperCallCount.get())
            argumentCaptor<Int>().apply {
                verify(loadCallback, atLeastOnce())
                Assert.assertEquals("Last value set is failed to load due to no connection",
                        STATUS_FAILED_NOCONNECTIVITYORPEERS, lastValue)
            }
        }
    }

    @Test
    fun givenLoadUnuccessfulWithNoWrappedLiveData_whenConnectivityAvailable_thenShouldNotLoadAgain() {
        val currentConnectivityStatus = AtomicInteger(DoorDatabaseRepository.STATUS_DISCONNECTED)
        val mockRepository = mock<DoorDatabaseRepository> {
            on {connectivityStatus}.thenAnswer { invocation -> currentConnectivityStatus.get() }
            onBlocking { activeMirrors() }.thenReturn(listOf())
        }

        val entity = DummyEntity(100, "Bob", "Jones")
        val completableDeferred = CompletableDeferred<DummyEntity>()

        val loadHelperCallCount = AtomicInteger()
        val repoLoadHelper = RepositoryLoadHelper<DummyEntity>(mockRepository,
                lifecycleHelperFactory = mock {  }) {endpoint ->
            loadHelperCallCount.incrementAndGet()
            if(currentConnectivityStatus.get() == DoorDatabaseRepository.STATUS_CONNECTED) {
                completableDeferred.complete(entity)
                entity
            }else {
                throw IOException("Mock IOException Not connected")
            }
        }
        val loadCallback = mock<RepositoryLoadHelper.RepoLoadCallback>()
        repoLoadHelper.addRepoLoadCallback(loadCallback)

        runBlocking {
            try {
                repoLoadHelper.doRequest()
            }catch(e: Exception) {
                //will fail because there is no connectivity
            }

            val callCountBeforeConnectivityRestored = loadHelperCallCount.get()
            currentConnectivityStatus.set(DoorDatabaseRepository.STATUS_CONNECTED)
            repoLoadHelper.onConnectivityStatusChanged(DoorDatabaseRepository.STATUS_CONNECTED)

            delay(2000)
            Assert.assertEquals("When there is no wrapped live data, the request is not retried when" +
                    "connectivity is restored", callCountBeforeConnectivityRestored, loadHelperCallCount.get())
            argumentCaptor<Int>().apply {
                verify(loadCallback, atLeastOnce()).onLoadStatusChanged(capture(), anyOrNull())
                Assert.assertEquals("Last status is failed due to no connection", STATUS_FAILED_NOCONNECTIVITYORPEERS,
                        lastValue)
            }
        }
    }


    @Test
    fun givenConnectivityAvailableAndMirrorAvailable_whenDoRequestCalled_thenWillUseMainEndpoint() {
        val mockCloudEndpoint = "http://cloudserver/endpoint"
        val mockMirrorEndpoint = "http://localhost:2000/proxy"
        val mockRepository = mock<DoorDatabaseRepository> {
            on {connectivityStatus}.thenReturn(DoorDatabaseRepository.STATUS_CONNECTED)
            on {endpoint}.thenReturn(mockCloudEndpoint)
            onBlocking { activeMirrors() }.thenReturn(listOf(MirrorEndpoint(1, mockMirrorEndpoint, 100)))
        }

        val entity = DummyEntity(100, "Bob", "Jones")
        val endpointUsed = CompletableDeferred<String>()
        val repoLoadHelper = RepositoryLoadHelper<DummyEntity>(mockRepository,
                lifecycleHelperFactory = mock {  }) {endpoint ->
            endpointUsed.complete(endpoint)
            entity
        }
        val loadCallback = mock<RepositoryLoadHelper.RepoLoadCallback>()
        repoLoadHelper.addRepoLoadCallback(loadCallback)

        runBlocking {
            repoLoadHelper.doRequest()
            val endpointUsedRef = withTimeout(5000)  { endpointUsed.await() }
            Assert.assertEquals("Given connectivity is available and mirror is available " +
                    "repoloadhelper uses main endpoint", mockCloudEndpoint, endpointUsedRef)

            verify(loadCallback, timeout(5000L)).onLoadStatusChanged(STATUS_LOADED_WITHDATA, null)
            argumentCaptor<Int>().apply {
                verify(loadCallback, atLeastOnce()).onLoadStatusChanged(capture(), anyOrNull())
                Assert.assertEquals("Last value set was loaded successfully with data",
                        STATUS_LOADED_WITHDATA, lastValue)
                Assert.assertTrue("Status was set to loading from cloud",
                        allValues.any { it == STATUS_LOADING_CLOUD})
                Assert.assertFalse("Status was not set to loading from local mirror",
                        allValues.any { it == STATUS_LOADING_MIRROR })
            }
        }
    }

    @Test
    fun givenNoConnectivityAvailableAndMirrorAvailable_whenDoRequestCalled_thenWillUseMirror() {
        val mockCloudEndpoint = "http://cloudserver/endpoint"
        val mockMirrorEndpoint = "http://localhost:2000/proxy"
        val mockRepository = mock<DoorDatabaseRepository> {
            on {connectivityStatus}.thenReturn(DoorDatabaseRepository.STATUS_DISCONNECTED)
            on {endpoint}.thenReturn(mockCloudEndpoint)
            onBlocking { activeMirrors() }.thenReturn(listOf(MirrorEndpoint(1, mockMirrorEndpoint, 100)))
        }

        val entity = DummyEntity(100, "Bob", "Jones")
        val endpointUsed = CompletableDeferred<String>()
        val repoLoadHelper = RepositoryLoadHelper<DummyEntity>(mockRepository,
                lifecycleHelperFactory = mock {  }) {endpoint ->
            endpointUsed.complete(endpoint)
            entity
        }
        val loadCallback = mock<RepositoryLoadHelper.RepoLoadCallback>()
        repoLoadHelper.addRepoLoadCallback(loadCallback)

        runBlocking {
            repoLoadHelper.doRequest()
            val endpointUsedRef = withTimeout(5000)  { endpointUsed.await() }
            Assert.assertEquals("Given connectivity is not available and mirror is available " +
                    "repoloadhelper uses mirror endpoint", mockMirrorEndpoint, endpointUsedRef)
            verify(loadCallback, timeout(5000)).onLoadStatusChanged(STATUS_LOADED_WITHDATA, null)
            argumentCaptor<Int>().apply {
                verify(loadCallback, atLeastOnce()).onLoadStatusChanged(capture(), anyOrNull())
                Assert.assertEquals("Last value set was loaded successfully with data",
                        STATUS_LOADED_WITHDATA, lastValue)
                Assert.assertTrue("Status was set to loading from mirror",
                        allValues.any { it == STATUS_LOADING_MIRROR})
                Assert.assertFalse("Status was not set to loading from cloud",
                        allValues.any { it == STATUS_LOADING_CLOUD })
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun givenRequestUnsuccessfulAndDataIsObserved_whenNewMirrorAvailable_thenWillRetry() {
        val mockCloudEndpoint = "http://cloudserver/endpoint"
        val mockMirrorEndpoint = "http://localhost:2000/proxy"

        val currentConnectivityStatus = AtomicInteger(DoorDatabaseRepository.STATUS_DISCONNECTED)
        val currentActiveMirrorList = mutableListOf<MirrorEndpoint>()
        val mockRepository = mock<DoorDatabaseRepository> {
            on {connectivityStatus}.thenAnswer { invocation -> currentConnectivityStatus.get() }
            on {endpoint}.thenReturn(mockCloudEndpoint)
            onBlocking { activeMirrors() }.thenAnswer { invocation -> currentActiveMirrorList }
        }

        val entity = DummyEntity(100, "Bob", "Jones")
        val endpointCompletableDeferred = CompletableDeferred<String>()
        val repoLoadHelper = RepositoryLoadHelper<DummyEntity>(mockRepository,
                lifecycleHelperFactory = mock {  }) {endpoint ->

            if(currentConnectivityStatus.get() == STATUS_CONNECTED || currentActiveMirrorList.isNotEmpty()) {
                endpointCompletableDeferred.complete(endpoint)
                entity
            }else {
                throw IOException("Mock offline and there is no mirror")
            }
        }
        val loadCallback = mock<RepositoryLoadHelper.RepoLoadCallback>()
        repoLoadHelper.addRepoLoadCallback(loadCallback)

        runBlocking {
            try {
                repoLoadHelper.doRequest()
            }catch(e: Exception) {
                //will fail
            }


            val endpointCompleteOnFirstRequest = endpointCompletableDeferred.isCompleted

            val mockLiveData = mock<DoorLiveData<DummyEntity>> {  }
            val wrappedLiveData = repoLoadHelper.wrapLiveData(mockLiveData)
                    as RepositoryLoadHelper<DummyEntity>.LiveDataWrapper<DummyEntity>
            wrappedLiveData.addActiveObserver(mock {})

            val newMirror = MirrorEndpoint(1, mockMirrorEndpoint, 100)
            currentActiveMirrorList.add(newMirror)

            repoLoadHelper.onNewMirrorAvailable(newMirror)

            val endpointUsed = withTimeout(5000) { endpointCompletableDeferred.await() }
            Assert.assertEquals("After a new mirror is available, and data is being observed, then " +
                    "the repoloadhelper automatically tries again using the new mirror",
                    mockMirrorEndpoint, endpointUsed)
            Assert.assertFalse("The repoloadhelper was not marked as complete when the request first loaded",
                    endpointCompleteOnFirstRequest)
            verify(loadCallback, timeout(5000)).onLoadStatusChanged(STATUS_LOADED_WITHDATA, null)
            argumentCaptor<Int>().apply {
                verify(loadCallback, atLeastOnce()).onLoadStatusChanged(capture(), anyOrNull())
                Assert.assertEquals("Last status value set is LOADED_WITH_DATA", STATUS_LOADED_WITHDATA,
                        lastValue)
            }
        }
    }

    @Test
    fun givenRequestUnsuccessfulAndDataIsNotObserved_whenNewMirrorAvailable_thenWillDoNothing() {
        val mockCloudEndpoint = "http://cloudserver/endpoint"
        val mockMirrorEndpoint = "http://localhost:2000/proxy"

        val currentConnectivityStatus = AtomicInteger(DoorDatabaseRepository.STATUS_DISCONNECTED)
        val currentActiveMirrorList = mutableListOf<MirrorEndpoint>()
        val mockRepository = mock<DoorDatabaseRepository> {
            on {connectivityStatus}.thenAnswer { invocation -> currentConnectivityStatus.get() }
            on {endpoint}.thenReturn(mockCloudEndpoint)
            onBlocking { activeMirrors() }.thenAnswer { invocation -> currentActiveMirrorList }
        }

        val entity = DummyEntity(100, "Bob", "Jones")
        val endpointCompletableDeferred = CompletableDeferred<String>()
        val loadFnCount = AtomicInteger()
        val repoLoadHelper = RepositoryLoadHelper<DummyEntity>(mockRepository,
                lifecycleHelperFactory = mock {  }) {endpoint ->

            loadFnCount.incrementAndGet()
            if(currentConnectivityStatus.get() == STATUS_CONNECTED || currentActiveMirrorList.isNotEmpty()) {
                endpointCompletableDeferred.complete(endpoint)
                entity
            }else {
                throw IOException("Mock offline and there is no mirror")
            }

        }

        runBlocking {
            try {
                repoLoadHelper.doRequest()
            }catch(e: Exception) {
                //will fail
            }

            val loadFnCountBeforeMirror = loadFnCount.get()

            val mockLiveData = mock<DoorLiveData<DummyEntity>> {  }
            val wrappedLiveData = repoLoadHelper.wrapLiveData(mockLiveData)
                    as RepositoryLoadHelper<DummyEntity>.LiveDataWrapper<DummyEntity>

            val newMirror = MirrorEndpoint(1, mockMirrorEndpoint, 100)
            currentActiveMirrorList.add(newMirror)

            repoLoadHelper.onNewMirrorAvailable(newMirror)

            val mirrorUsed = withTimeoutOrNull(5000) { endpointCompletableDeferred.await() }
            Assert.assertNull("After a new mirror is available, when data is not being " +
                    "observed the loadhelper will not try again",
                    mirrorUsed)
            Assert.assertEquals("DoRequest loader function has not been called again",
                    loadFnCountBeforeMirror, loadFnCount.get())
        }
    }


}
