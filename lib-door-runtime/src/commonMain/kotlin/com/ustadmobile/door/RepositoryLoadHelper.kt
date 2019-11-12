package com.ustadmobile.door

import kotlinx.coroutines.*
import kotlinx.io.IOException
import kotlin.coroutines.coroutineContext
import kotlin.jvm.Volatile

//Empty / null retry:
// On DataSourceFactory / boundary callback: alwasy - because it was called by onZeroItemsLoaded
// On LiveData - never - use a reference to the livedata itself, and check if it's null or empty
// On a normal or suspended return type: never. THe generated code has to check the result and call again if needed
// e.g.
/**
 * @param autoRetryEmptyMirrorResult - if true, this assumes that an empty result from a mirror means
 * it did not have the data we were looking for. This is useful for BoundaryCallback loads, which
 * are themselves triggered by the database not having data.
 */
class RepositoryLoadHelper<T>(val repository: DoorDatabaseRepository,
                              val autoRetryEmptyMirrorResult: Boolean = false,
                              val maxAttempts: Int = 3,
                              val retryDelay: Int = 5000,
                              val autoRetryOnEmptyLiveData: DoorLiveData<T>? = null,
                              val loadFn: suspend(endpoint: String) -> T) {

    var liveDataWrapper: LiveDataWrapper<*>? = null

    /**
     * This wrapper exists to monitor when LiveData is actively observed. The repository will wrap
     * the return type so that we can watch if the data is being observed.
     */
    inner class LiveDataWrapper<L>(private val src: DoorLiveData<L>,
                                   internal var onActiveCb: (()-> Unit)? = null) : DoorLiveData<L>() {

        val dummyObserverMap = mutableMapOf<DoorObserver<in L>, DoorObserver<in L>>()

        inner class DummyObserver<L>: DoorObserver<L> {
            override fun onChanged(t: L) {
                //do nothing
            }
        }

        override fun observe(lifecycleOwner: DoorLifecycleOwner, observer: DoorObserver<in L>) {
            src.observe(lifecycleOwner, observer)
            val dummyObserver = DummyObserver<L>()
            dummyObserverMap[observer] = dummyObserver
            super.observe(lifecycleOwner, dummyObserver)
        }

        override fun observeForever(observer: DoorObserver<in L>) {
            src.observeForever(observer)
            val dummyObserver = DummyObserver<L>()
            dummyObserverMap[observer] = dummyObserver
            super.observeForever(dummyObserver)
        }

        override fun removeObserver(observer: DoorObserver<in L>) {
            src.removeObserver(observer)
            dummyObserverMap.remove(observer)
        }
    }

    fun <L> wrapLiveData(src: DoorLiveData<L>): DoorLiveData<L> {
        liveDataWrapper?.onActiveCb = null
        val newWrapper = LiveDataWrapper<L>(src)
        liveDataWrapper = newWrapper
        return newWrapper
    }

    @Volatile
    var completed = false

    @Volatile
    var triedMainEndpoint = false

    private val mirrorsTried = mutableListOf<Int>()

    @Volatile
    var attemptCount = 0

    suspend fun doRequest() : T{
        do {
            var mirrorToUse: MirrorEndpoint? = null
            var endpointToUse: String? = null
            try {
                attemptCount++
                val isConnected = repository.connectivityStatus == DoorDatabaseRepository.STATUS_CONNECTED
                mirrorToUse = if(isConnected && !triedMainEndpoint) {
                    null as MirrorEndpoint? //use the main endpoint
                }else {
                    repository.activeMirrors().firstOrNull { it.mirrorId !in mirrorsTried }
                }

                endpointToUse = if(mirrorToUse == null) {
                    repository.endpoint
                }else {
                    mirrorToUse.endpointUrl
                }

                var t = loadFn(endpointToUse)
                val isNullOrEmpty = if(t is List<*>) {
                    t.isEmpty()
                }else {
                    t == null
                }

                //if it came from the main endpoint, or we got some actual data, then it looks good
                var isMainEndpointOrNotNullOrEmpty = mirrorToUse == null || !isNullOrEmpty
                if(isMainEndpointOrNotNullOrEmpty) {
                    completed = true
                }

                if(!completed && autoRetryOnEmptyLiveData != null) {
                    val liveDataVal = waitForNonEmptyLiveData()
                    if(liveDataVal != null) {
                        t = liveDataVal
                        isMainEndpointOrNotNullOrEmpty = true
                        completed = true
                    }
                }

                if(isMainEndpointOrNotNullOrEmpty || !autoRetryEmptyMirrorResult) {
                    return t
                }

                delay(retryDelay.toLong())
            }catch(e: Exception) {
                //something went wrong with the load
                println("RepositoryLoadHelper: Exception attempting to load from $endpointToUse: $e")
            }

            if(mirrorToUse == null) {
                triedMainEndpoint = true
            }else {
                mirrorsTried.add(mirrorToUse.mirrorId)
            }
        }while(coroutineContext.isActive && attemptCount <= maxAttempts)

        throw IOException("loadHelper retry count exceeded")
    }

    fun shouldTryAnotherMirror() : Boolean {
        return !completed && attemptCount < maxAttempts
    }

    suspend fun waitForNonEmptyLiveData() : T?{
        val completableDeferred = CompletableDeferred<T>()
        val observer = object: DoorObserver<T> {
            override fun onChanged(t: T) {
                if(t is List<*> && t.isNotEmpty()) {
                    completableDeferred.complete(t)
                }else if(t !is List<*> && t != null){
                    completableDeferred.complete(t)
                }
            }
        }

        var nonEmptyVal: T? = null
        withContext(liveDataObserverDispatcher()) {
            autoRetryOnEmptyLiveData?.observeForever(observer)
            nonEmptyVal = withTimeoutOrNull(500) { completableDeferred.await()}
            autoRetryOnEmptyLiveData?.removeObserver(observer)
        }

        return nonEmptyVal
    }

    companion object {
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
    }

}