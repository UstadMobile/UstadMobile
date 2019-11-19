package com.ustadmobile.door

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import com.ustadmobile.door.RepositoryConnectivityListener
import java.util.WeakHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ConcurrentHashMap
import java.lang.ref.WeakReference
import com.github.aakira.napier.Napier

/**
 * This implements common repository functions such as addMirror, removeMirror, setMirrorPriority
 * setConnectivityStatus and val connectivityStatus
 */
class RepositoryHelper(private val coroutineDispatcher: CoroutineDispatcher = liveDataObserverDispatcher()) {

    private val mirrors: MutableMap<Int, MirrorEndpoint> = ConcurrentHashMap()

    private val nextMirrorId = AtomicInteger(1)

    private val connectivityStatusAtomic = AtomicInteger(0)

    private val weakConnectivityListeners: MutableList<WeakReference<RepositoryConnectivityListener>> = CopyOnWriteArrayList()

    var connectivityStatus: Int
        get() = connectivityStatusAtomic.get()
        set(newValue) {
            connectivityStatusAtomic.set(newValue)
            weakConnectivityListeners.forEach {
                it.get()?.onConnectivityStatusChanged(newValue)
            }
        }

    suspend fun addMirror(mirrorEndpoint: String, initialPriority: Int) = withContext(coroutineDispatcher){
        val newMirror = MirrorEndpoint(nextMirrorId.incrementAndGet(), mirrorEndpoint, initialPriority)
        mirrors[newMirror.mirrorId] = newMirror
        Napier.i("RepositoryHelper: New mirror added #${newMirror.mirrorId} - $mirrorEndpoint")

        weakConnectivityListeners.forEach {
            it.get()?.onNewMirrorAvailable(newMirror)
        }

        newMirror.mirrorId
    }

    suspend fun removeMirror(mirrorId: Int) = withContext(coroutineDispatcher) {
        mirrors.remove(mirrorId)
    }

    suspend fun updateMirrorPriorities(newPriorities: Map<Int, Int>) = withContext(coroutineDispatcher) {
        newPriorities.forEach {
            mirrors[it.key]?.priority = it.value
        }
    }

    suspend fun activeMirrors() = mirrors.values.toList()


    fun addWeakConnectivityListener(listener: RepositoryConnectivityListener) {
        weakConnectivityListeners.add(WeakReference(listener))
    }

    fun removeWeakConnectivityListener(listener: RepositoryConnectivityListener) {
        val list = mutableListOf<Int>()
        weakConnectivityListeners.removeAll { it.get() == listener }
    }

}