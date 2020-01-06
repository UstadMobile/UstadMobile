package com.ustadmobile.door

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlin.coroutines.coroutineContext
import kotlin.jvm.Volatile
import com.github.aakira.napier.Napier
import com.ustadmobile.door.util.threadSafeListOf

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
                              val lifecycleHelperFactory: (DoorLifecycleOwner) -> RepositoryLoadHelperLifecycleHelper =
                                      {RepositoryLoadHelperLifecycleHelper(it)},
                              val uri: String = "",
                              val loadFn: suspend(endpoint: String) -> T) : RepositoryConnectivityListener {


    interface RepoLoadCallback {

        fun onLoadStatusChanged(status: Int, remoteDevice: String?)

    }

    var liveDataWrapper: LiveDataWrapper<*>? = null

    val requestLock = Mutex()

    val loadedVal = CompletableDeferred<T>()

    val repoHelperId = ID_ATOMICINT.getAndIncrement()

    private val callbacks = threadSafeListOf<RepoLoadCallback>()

    @Volatile
    var status: Int = 0
        private set

    init {
        repository.addWeakConnectivityListener(this)
    }

    private val logPrefix
        get() = "ID [$uri] $repoHelperId "

    /**
     * This wrapper exists to monitor when LiveData is actively observed. The repository will wrap
     * the return type so that we can watch if the data is being observed.
     */
    inner class LiveDataWrapper<L>(private val src: DoorLiveData<L>,
                                   internal var onActiveCb: (suspend ()-> Unit)? = null) : DoorLiveData<L>() {

        val observerHelpersMap
                = mutableMapOf<DoorObserver<in L>, RepositoryLoadHelperLifecycleHelper>()

        val activeObservers = mutableListOf<DoorObserver<in L>>()

        val active = atomic(false)

        override fun observe(lifecycleOwner: DoorLifecycleOwner, observer: DoorObserver<in L>) {
            val lifecycleHelper = lifecycleHelperFactory(lifecycleOwner)
            observerHelpersMap[observer] = lifecycleHelper
            lifecycleHelper.onActive = { addActiveObserver(observer) }
            lifecycleHelper.onInactive = { removeActiveObserver(observer) }
            lifecycleHelper.addObserver()

            src.observe(lifecycleOwner, observer)
        }

        internal fun addActiveObserver(observer: DoorObserver<in L>) {
            activeObservers.add(observer)
            val numObservers = activeObservers.size
            if(numObservers == 1) {
                active.value = activeObservers.isNotEmpty()
                if(!loadedVal.isCompleted) {
                    //try again if needed
                    attemptCount = 0
                    GlobalScope.launch {
                        try {
                            Napier.d("$logPrefix : addActiveObserver: did not complete " +
                                            "and data is being observed. Trying again.")
                            onActiveCb?.invoke()
                        } catch(e: Exception) {
                            Napier.e("$logPrefix : addActiveObserver: ERROR " +
                                    "did not complete and data is being observed: ", e)
                        }
                    }
                }
            }
        }

        internal fun removeActiveObserver(observer: DoorObserver<in L>) {
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
        val newWrapper = LiveDataWrapper<L>(src) {
            try {
                doRequest(resetAttemptCount = true)
            }catch(e: Exception) {
                Napier.e("$logPrefix Exception running LiveDataWrapper.wrapLiveData callback", e)
            }
        }
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
                && liveDataWrapper?.active?.value ?: false) {
            GlobalScope.launch {
                try {
                    Napier.d("$logPrefix RepositoryLoadHelper: onConnectivityStatusChanged: did not complete " +
                                    "and data is being observed. Trying again.")
                    doRequest(resetAttemptCount = true)
                } catch (e: Exception) {
                    Napier.e("$logPrefix RepositoryLoadHelper: onConnectivityStatusChanged: ERROR " +
                                    "did not complete and data is being observed: ", e)
                }
            }
        }
    }

    override fun onNewMirrorAvailable(mirror: MirrorEndpoint) {
        if(!completed.value && liveDataWrapper?.active?.value ?: true) {
            GlobalScope.launch {
                try {
                    Napier.d("$logPrefix RepositoryLoadHelper: onNewMirrorAvailable: Mirror # ${mirror.mirrorId} " +
                                    "did not complete and data is being observed. Trying again.")
                    doRequest(resetAttemptCount = true)
                } catch(e: Exception) {
                    Napier.e("$logPrefix RepositoryLoadHelper: onNewMirrorAvailable: ERROR " +
                                    "did not complete and data is being observed: ", e)
                }
            }
        }
    }

    suspend fun doRequest(resetAttemptCount: Boolean = false) : T{
        requestLock.withLock {
            Napier.d("$logPrefix doRequest: resetAttemptCount = $resetAttemptCount")
            if(resetAttemptCount) {
                attemptCount = 0
                triedMainEndpoint = false
            }
            var mirrorToUse: MirrorEndpoint? = null
            while(!completed.value && coroutineContext.isActive && attemptCount <= maxAttempts) {
                var endpointToUse: String? = null
                try {
                    attemptCount++
                    val isConnected = repository.connectivityStatus == DoorDatabaseRepository.STATUS_CONNECTED
                    mirrorToUse = if(isConnected && !triedMainEndpoint) {
                        null as MirrorEndpoint? //use the main endpoint
                    }else {
                        repository.activeMirrors().maxBy { it.priority }
                    }

                    if(!isConnected && mirrorToUse == null) {
                        //it's hopeless - there is no mirror and we have no connection - give up
                        throw IOException("$PREFIX_NOCONNECTION_NO_MIRRORS_MESSAGE $logPrefix: " +
                                "Repository status indicates no connectivity and there are no active " +
                                "mirrors")
                    }

                    val newStatus = if(mirrorToUse != null) {
                        STATUS_LOADING_MIRROR
                    }else {
                        STATUS_LOADING_CLOUD
                    }

                    if(newStatus != status) {
                        status = newStatus
                        fireStatusChanged(status, null)
                    }

                    endpointToUse = if(mirrorToUse == null) {
                        repository.endpoint
                    }else {
                        mirrorToUse.endpointUrl
                    }

                    Napier.d({"$logPrefix doRequest: calling loadFn using endpoint $endpointToUse ."})
                    var t = loadFn(endpointToUse)
                    val isNullOrEmpty = if(t is List<*>) {
                        t.isEmpty()
                    }else {
                        t == null
                    }

                    //if it came from the main endpoint, or we got some actual data, then it looks good
                    var isMainEndpointOrNotNullOrEmpty = mirrorToUse == null || !isNullOrEmpty
                    if(isMainEndpointOrNotNullOrEmpty) {
                        //completed.value = true
                    }

                    if(!isMainEndpointOrNotNullOrEmpty && autoRetryOnEmptyLiveData != null) {
                        val liveDataVal = waitForNonEmptyLiveData()
                        if(liveDataVal != null) {
                            t = liveDataVal
                            isMainEndpointOrNotNullOrEmpty = true
                            //completed.value = true
                        }
                    }

                    if(isMainEndpointOrNotNullOrEmpty || !autoRetryEmptyMirrorResult) {
                        status = if(isNullOrEmpty) {
                            STATUS_LOADED_NODATA
                        }else {
                            STATUS_LOADED_WITHDATA
                        }

                        completed.value = true
                        loadedVal.complete(t)
                        fireStatusChanged(status, null)

                        Napier.d({"$logPrefix doRequest: completed successfully from $endpointToUse ."})
                        return t
                    }else {
                        Napier.e({"$logPrefix doRequest: loadFn completed from $endpointToUse but " +
                                "not successful. IsNullOrEmpty=$isNullOrEmpty, " +
                                "autoRetryOnEmptyLiveData=${autoRetryOnEmptyLiveData != null}" +
                                "autoRetryEmptyMirrorResult=$autoRetryEmptyMirrorResult"})
                    }

                    delay(retryDelay.toLong())
                }catch(e: Exception) {
                    //something went wrong with the load
                    Napier.e("$logPrefix Exception attempting to load from $endpointToUse",
                            e)
                    if(e.message?.startsWith(PREFIX_NOCONNECTION_NO_MIRRORS_MESSAGE) ?: false) {
                        Napier.d({"No connection and no mirrors available - giving up"})
                        break
                    }
                }

                if(mirrorToUse == null) {
                    triedMainEndpoint = true
                }else {
                    mirrorsTried.add(mirrorToUse.mirrorId)
                }
            }

            Napier.d("$logPrefix doRequest: over. Is completed=${loadedVal.isCompleted}")
            if(loadedVal.isCompleted) {
                return loadedVal.getCompleted()
            }else {
                val isConnected = repository.connectivityStatus == DoorDatabaseRepository.STATUS_CONNECTED
                val newStatus = if(isConnected || mirrorToUse != null) {
                    STATUS_FAILED_CONNECTION_ERR
                }else {
                    STATUS_FAILED_NOCONNECTIVITYORPEERS
                }

                if(newStatus != status) {
                    status = newStatus
                    fireStatusChanged(status, null)
                }


                throw IOException("$logPrefix ==ERROR== NOT completed")
            }
        }
    }

    fun shouldTryAnotherMirror() : Boolean {
        val isCompleted = completed.value
        return !isCompleted && attemptCount < maxAttempts
    }

    suspend fun waitForNonEmptyLiveData() : T?{
        val completableDeferred = CompletableDeferred<T>()
        Napier.d("$logPrefix waiting for non empty live data.")
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

        Napier.d({"$logPrefix Finished waiting for non empty live data. Result=$nonEmptyVal."})

        return nonEmptyVal
    }

    fun addRepoLoadCallback(callback: RepoLoadCallback) {
        callbacks.add(callback)
        callback.onLoadStatusChanged(status, null)
    }

    inline fun addRepoLoadCallback(crossinline block: (Int, String?) -> Unit) {
        addRepoLoadCallback(object: RepoLoadCallback {
            override fun onLoadStatusChanged(status: Int, remoteDevice: String?) {
                block(status, remoteDevice)
            }
        })
    }

    fun removeRepoLoadCallback(callback: RepoLoadCallback) = callbacks.remove(callback)

    private fun fireStatusChanged(status: Int, remoteDevice: String?) {
        callbacks.forEach { it.onLoadStatusChanged(status, remoteDevice) }
    }

    companion object {

        private const val PREFIX_NOCONNECTION_NO_MIRRORS_MESSAGE = "LoadHelper-NOCONNECTION"

        val ID_ATOMICINT = atomic(0)

        const val STATUS_LOADING_CLOUD = 1

        const val STATUS_LOADING_MIRROR = 2

        const val STATUS_LOADED_WITHDATA = 11

        const val STATUS_LOADED_NODATA = 12

        const val STATUS_FAILED_NOCONNECTIVITYORPEERS = 15

        const val STATUS_FAILED_CONNECTION_ERR = 16

    }
}