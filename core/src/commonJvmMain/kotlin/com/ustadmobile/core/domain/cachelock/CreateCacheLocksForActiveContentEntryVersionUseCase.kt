package com.ustadmobile.core.domain.cachelock

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.contententry.importcontent.CreateRetentionLocksForManifestUseCase
import com.ustadmobile.core.util.ext.bodyAsDecodedText
import com.ustadmobile.door.room.InvalidationTrackerObserver
import com.ustadmobile.lib.db.entities.CacheLockJoin
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.Closeable

/**
 * Create a CacheLockJoin entity for all current ContentEntryVersion entities to retain all
 * the urls referenced by the manifest - the ContentManifestEntry.bodyDataUrl for
 * ContentManifestEntry and the url of the manifest itself. This is only applicable where the
 * manifest is stored on the same given endpoint. If the manifest (and data) are stored on a
 * different server, it will only be retained on that server.
 *
 * This is used on the server to ensure that the binary data required for content is not evicted
 * from the cache. This is similar to UpdateCacheLockJoinUseCase, but differs in two ways:
 *
 *   1) The urls for each entry in the manifest are not stored in the database, we need to read and
 *      parse the manifest to get the URLs.
 *   2) The urls might not be stored on this server, in which case we are not obliged to retain them.
 */
class CreateCacheLocksForActiveContentEntryVersionUseCase(
    private val db: UmAppDatabase,
    private val httpClient: HttpClient,
    private val json: Json,
    private val learningSpace: LearningSpace,
    private val createRetentionLocksForManifestUseCase: CreateRetentionLocksForManifestUseCase,
): Closeable {

    private val signalChannel = Channel<Unit>(
        capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val observer: InvalidationTrackerObserver = object: InvalidationTrackerObserver(
        arrayOf("ContentEntryVersion")
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

    suspend operator fun invoke(){
        val versionsWithoutLocks = db.contentEntryVersionDao()
            .findContentEntryVersionsWithoutCacheLock()

        val cacheLockJoins = versionsWithoutLocks.flatMap { contentEntryVersion ->
            val manifestUrl = contentEntryVersion.cevManifestUrl
            if(manifestUrl != null && manifestUrl.startsWith(learningSpace.url)) {
                val manifest: ContentManifest = json.decodeFromString(
                    httpClient.get(manifestUrl).bodyAsDecodedText()
                )
                val locksCreated = createRetentionLocksForManifestUseCase(
                    contentEntryVersionUid = contentEntryVersion.cevUid,
                    manifestUrl = manifestUrl,
                    manifest = manifest,
                )

                locksCreated.map {
                    CacheLockJoin(
                        cljTableId = ContentEntryVersion.TABLE_ID,
                        cljEntityUid = contentEntryVersion.cevUid,
                        cljLockId = it.lockId,
                        cljUrl = it.url,
                        cljStatus = CacheLockJoin.STATUS_CREATED,
                        cljType = CacheLockJoin.TYPE_SERVER_RETENTION,
                    )
                }
            }else {
                emptyList()
            }
        }

        db.cacheLockJoinDao().insertListAsync(cacheLockJoins)
    }

    override fun close(){
        signalChannel.close()
        scope.cancel()
    }
}