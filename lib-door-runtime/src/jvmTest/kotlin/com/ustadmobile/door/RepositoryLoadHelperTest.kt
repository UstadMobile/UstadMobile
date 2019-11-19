package com.ustadmobile.door

import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.door.DoorDatabaseRepository.Companion.STATUS_CONNECTED
import kotlinx.coroutines.*
import org.junit.Assert
import org.junit.Test
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger


class RepositoryLoadHelperTest  {

    data class DummyEntity(val uid: Long, val firstName: String, val lastName: String)

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

        runBlocking {
            repoLoadHelper.doRequest()
            repoLoadHelper.doRequest()

            Assert.assertEquals("RepoLoadHelper only calls the actual request once if the " +
                    "request was successful" ,
                    1, invocationCount.get())
        }
    }

    @Test
    fun givenLoadUnsuccessfulWithNoConnectivityAndIsNotObserved_whenConnectivityResumed_thenShouldNotLoadAgain() {
        val currentConnectivityStatus = AtomicInteger(DoorDatabaseRepository.STATUS_DISCONNECTED)
        val mockRepository = mock<DoorDatabaseRepository> {
            on {connectivityStatus}.thenReturn(currentConnectivityStatus.get())
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
        }
    }


    @Test
    fun givenLoadUnsuccessful_whenObservedAgainAndConnectivityAvailable_thenShouldLoadAgain() {
        val currentConnectivityStatus = AtomicInteger(DoorDatabaseRepository.STATUS_DISCONNECTED)
        val mockRepository = mock<DoorDatabaseRepository> {
            on {connectivityStatus}.thenAnswer { invocation -> currentConnectivityStatus.get() }
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
        }
    }

    @Test
    fun givenLoadUnsucessful_whenObservedAgainAndNoConnectivityAvailable_thenShouldNotLoadAgain() {
        val mockRepository = mock<DoorDatabaseRepository> {
            on {connectivityStatus}.thenReturn(DoorDatabaseRepository.STATUS_DISCONNECTED)
        }


        val loadHelperCallCount = AtomicInteger()
        val repoLoadHelper = RepositoryLoadHelper<DummyEntity>(mockRepository,
                lifecycleHelperFactory = mock {  }) {endpoint ->
            loadHelperCallCount.incrementAndGet()
            throw IOException("Mock IOException Not connected")
        }

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
        }
    }

    @Test
    fun givenLoadUnuccessfulWithNoWrappedLiveData_whenConnectivityAvailable_thenShouldNotLoadAgain() {
        val currentConnectivityStatus = AtomicInteger(DoorDatabaseRepository.STATUS_DISCONNECTED)
        val mockRepository = mock<DoorDatabaseRepository> {
            on {connectivityStatus}.thenAnswer { invocation -> currentConnectivityStatus.get() }
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

        runBlocking {
            repoLoadHelper.doRequest()
            val endpointUsedRef = withTimeout(5000)  { endpointUsed.await() }
            Assert.assertEquals("Given connectivity is available and mirror is available " +
                    "repoloadhelper uses main endpoint", mockCloudEndpoint, endpointUsedRef)
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

        runBlocking {
            repoLoadHelper.doRequest()
            val endpointUsedRef = withTimeout(5000)  { endpointUsed.await() }
            Assert.assertEquals("Given connectivity is not available and mirror is available " +
                    "repoloadhelper uses mirror endpoint", mockMirrorEndpoint, endpointUsedRef)
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
