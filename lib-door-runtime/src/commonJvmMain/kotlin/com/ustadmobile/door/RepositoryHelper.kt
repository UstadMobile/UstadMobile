package com.ustadmobile.door

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
/**
 * This implements common repository functions such as addMirror, removeMirror, setMirrorPriority
 * setConnectivityStatus and val connectivityStatus
 */
class RepositoryHelper(private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default) {

    private val mirrors: MutableMap<Int, MirrorEndpoint> = mutableMapOf()

    private var nextMirrorId = 1

    private val connectivityStatusAtomic = AtomicInteger(0)

    var connectivityStatus: Int
        get() = connectivityStatusAtomic.get()
        set(newValue) = connectivityStatusAtomic.set(newValue)

    suspend fun addMirror(mirrorEndpoint: String, initialPriority: Int) = withContext(coroutineDispatcher){
        val newMirror = MirrorEndpoint(nextMirrorId++, mirrorEndpoint, initialPriority)
        mirrors[nextMirrorId] = newMirror
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





}