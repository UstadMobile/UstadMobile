package com.ustadmobile.core.domain.cachelock

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.doorIdentityHashCode
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.room.InvalidationTrackerObserver
import com.ustadmobile.lib.db.entities.CacheLockJoin
import com.ustadmobile.libcache.EntryLockRequest
import com.ustadmobile.libcache.RemoveLockRequest
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.md5.Md5Digest
import com.ustadmobile.libcache.md5.urlKey
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Binary data is stored in the cache provided by lib-cache. The server must automatically retain
 * all URLs referenced by current active entities for certain tables (e.g. pictures, assignment
 * file submissions, current content entry files, etc). Android and desktop clients must retain URLs
 * referenced by entities that have been selected for offline access by the user (they must also
 * retain urls referenced by entities when they are pending upload, but this is handled by
 * SaveLocalUrisAsBlobsUseCase).
 *
 * The cache is a separate database (not least because it is used by an interceptor for the
 * system-wide OKHttp instance).
 *
 * This is done by adding a RetentionLock to the cache to ensure that the data an url is not evicted
 * whilst it remains actively referenced, and can be evicted once it is no longer referenced.
 *
 * Triggers can be used to insert and update the CacheLockJoin entity. When a new URL is referenced
 * (either by creation of a new entity or updating an existing entity to a new URL value) a
 * CacheLockJoin entity can be created with the status set to STATUS_PENDING_CREATION. When a URL
 * is no longer referenced (because the entity was updated to a new value, or deleted) then the
 * CacheLockJoin entity can status should be set to STATUS_PENDING_DELETE.
 *
 * This case will be instantiated by the DI on platforms that use lib-cache (server, desktop, Android)
 * at the time the database is created for any given endpoint scope. It will then observe changes
 * to the CacheLockJoin table. It will then create locks for any CacheLockJoin(s) that are pending
 * creation and remove locks for any that are pending deletion.
 */
class UpdateCacheLockJoinUseCase(
    private val db: UmAppDatabase,
    private val cache: UstadCache
) {

    private val logPrefix = "UpdateCacheLockJoinUseCase(${this.doorIdentityHashCode}):"

    private val signalChannel = Channel<Unit>(
        capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val observer: InvalidationTrackerObserver = object: InvalidationTrackerObserver(
        arrayOf("CacheLockJoin")
    ) {
        override fun onInvalidated(tables: Set<String>) {
            signalChannel.trySend(Unit)
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    init {
        db.invalidationTracker.addObserver(observer)
        scope.launch {
            for(signal in signalChannel) {
                invoke()
            }
        }
    }

    suspend operator fun invoke() {
        val md5Digest = Md5Digest()
        Napier.v { "$logPrefix checking for pending lock changes" }

        db.withDoorTransactionAsync {
            val pendingLocks = db.cacheLockJoinDao().findPendingLocks()

            if(pendingLocks.isEmpty())
                return@withDoorTransactionAsync

            val locksToDelete = pendingLocks.filter {
                it.cljStatus == CacheLockJoin.STATUS_PENDING_DELETE
            }

            if(locksToDelete.isNotEmpty()) {
                Napier.v { "$logPrefix creating locks for ${locksToDelete.joinToString { it.cljUrl ?: "" } }" }
                cache.removeRetentionLocks(
                    locksToDelete.mapNotNull {  cacheLockJoin ->
                        cacheLockJoin.cljUrl?.let { cacheLockJoinUrl ->
                            RemoveLockRequest(cacheLockJoinUrl, cacheLockJoin.cljLockId)
                        }
                    }
                )
                db.cacheLockJoinDao().deleteListAsync(locksToDelete)
            }

            val createLockRequests = pendingLocks.filter {
                it.cljStatus == CacheLockJoin.STATUS_PENDING_CREATION
            }

            if(createLockRequests.isNotEmpty()) {
                Napier.v { "$logPrefix creating locks for ${createLockRequests.joinToString { it.cljUrl ?: ""} } " }
                val createdLocks = cache.addRetentionLocks(
                    createLockRequests.map { EntryLockRequest(url = it.cljUrl!!) }
                ).associateBy { it.second.lockKey }

                createLockRequests.forEach { createLockRequest ->
                    val urlKey = md5Digest.urlKey(createLockRequest.cljUrl!!)
                    val lockId = createdLocks[urlKey]?.second?.lockId ?: 0
                    db.cacheLockJoinDao().updateLockIdAndStatus(
                        uid = createLockRequest.cljId,
                        lockId = lockId,
                        status = if(lockId != 0L)
                            CacheLockJoin.STATUS_CREATED
                        else
                            CacheLockJoin.STATUS_ERROR
                    )
                }
            }
        }

        Napier.v { "$logPrefix checking for pending lock changes: done" }
    }

    fun close() {
        db.invalidationTracker.removeObserver(observer)
        signalChannel.close()
        scope.cancel()
    }

}