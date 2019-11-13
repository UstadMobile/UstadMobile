package com.ustadmobile.door

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
                              val loadFn: suspend(endpoint: String) -> T) : RepositoryConnectivityListener {

    var liveDataWrapper: LiveDataWrapper<*>? = null

    val requestLock = Mutex()

    init {
        repository.addWeakConnectivityListener(this)
    }

    /**
     * This wrapper exists to monitor when LiveData is actively observed. The repository will wrap
     * the return type so that we can watch if the data is being observed.
     */
    inner class LiveDataWrapper<L>(private val src: DoorLiveData<L>,
                                   internal var onActiveCb: (()-> Unit)? = null) : DoorLiveData<L>() {

        val observerHelpersMap
                = mutableMapOf<DoorObserver<in L>, RepositoryLoadHelperLifecycleHelper>()

        val activeObservers = mutableListOf<DoorObserver<in L>>()

        val active = atomic(false)

        override fun observe(lifecycleOwner: DoorLifecycleOwner, observer: DoorObserver<in L>) {
            val lifecycleHelper = RepositoryLoadHelperLifecycleHelper(
                    this@RepositoryLoadHelper, lifecycleOwner)
            observerHelpersMap[observer] = lifecycleHelper
            lifecycleHelper.onActive = { addActiveObserver(observer) }
            lifecycleHelper.onInactive = { removeActiveObserver(observer) }
            lifecycleHelper.addObserver()

            src.observe(lifecycleOwner, observer)
        }

        private fun addActiveObserver(observer: DoorObserver<in L>) {
            activeObservers.add(observer)
            val numObservers = activeObservers.size
            if(numObservers == 1) {
                active.value = activeObservers.isNotEmpty()
                if(!completed.value) {
                    //try again if needed
                    attemptCount = 0
                    GlobalScope.launch {
                        try {
                            doRequest()
                        } catch(e: IOException) {

                        }
                    }
                }
            }
        }

        private fun removeActiveObserver(observer: DoorObserver<in L>) {
            activeObservers.remove(observer)
            active.value = activeObservers.isNotEmpty()
        }

        override fun observeForever(observer: DoorObserver<in L>) {
            src.observeForever(observer)
            addActiveObserver(observer)
        }

        override fun removeObserver(observer: DoorObserver<in L>) {
            src.removeObserver(observer)
            val observerHelper = observerHelpersMap[observer]
            if(observerHelper != null) {
                observerHelper.removeObserver()
                observerHelpersMap.remove(observer)
            }

            if(observer in activeObservers) {
                removeActiveObserver(observer)
            }
        }
    }

    fun <L> wrapLiveData(src: DoorLiveData<L>): DoorLiveData<L> {
        liveDataWrapper?.onActiveCb = null
        val newWrapper = LiveDataWrapper<L>(src)
        liveDataWrapper = newWrapper
        return newWrapper
    }

    val completed = atomic(false)

    @Volatile
    var triedMainEndpoint = false

    private val mirrorsTried = mutableListOf<Int>()

    @Volatile
    var attemptCount = 0

    override fun onConnectivityStatusChanged(newStatus: Int) {
        if(!completed.value && newStatus == DoorDatabaseRepository.STATUS_CONNECTED
                && liveDataWrapper?.active?.value ?: true)
            attemptCount = 0
            GlobalScope.launch {
                try {
                    doRequest()
                } catch(e: IOException) {

                }
            }
    }

    override fun onNewMirrorAvailable(mirror: MirrorEndpoint) {
        if(!completed.value && liveDataWrapper?.active?.value ?: true) {
            GlobalScope.launch {
                try {
                    doRequest()
                } catch(e: IOException) {

                }
            }
        }
    }

    suspend fun doRequest() : T{
        requestLock.withLock {
            while(!completed.value && coroutineContext.isActive && attemptCount <= maxAttempts) {
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

                    if(!isConnected && mirrorToUse == null) {
                        //it's hopeless - there is no mirror and we have no connection - give up
                        throw IOException("LoadHelper: Repository status indicates no connectivity and there are no active mirrors")
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
                        completed.value = true
                    }

                    if(!completed.value && autoRetryOnEmptyLiveData != null) {
                        val liveDataVal = waitForNonEmptyLiveData()
                        if(liveDataVal != null) {
                            t = liveDataVal
                            isMainEndpointOrNotNullOrEmpty = true
                            completed.value = true
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
            }

            throw IOException("loadHelper retry count exceeded")
        }
    }

    fun shouldTryAnotherMirror() : Boolean {
        val isCompleted = completed.value
        return !isCompleted && attemptCount < maxAttempts
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