package com.ustadmobile.core.contentjob

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.downloadUrlIfRemote
import com.ustadmobile.core.io.ext.emptyRecursively
import com.ustadmobile.core.io.ext.isRemote
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.door.DoorUri
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.kodein.di.DI
import org.kodein.di.DIAware
import kotlin.jvm.Volatile

/**
 * ContentPlugins are created as singletons. The functions may be called concurrently to process
 * different URIs. The ContentJobProcessContext can be used to hold information that is specific
 * to a given source URI being processed. It can also help avoid duplicate work (e.g. downloading a
 * remote URI to a temporary folder). When multiple plugins are checking a single URI (e.g. checking
 * to extract meta data), a single ContentJobProcessContext will be created.
 */
class ContentJobProcessContext(
    /**
     * The actual URI that is being processed (could be remote or local)
     */
    private val srcUri: DoorUri,

    /**
     * A temporary directory. On Android/JVM this URI MUST represent a file
     */
    val tempDirUri: DoorUri,

    /**
     * A map of params that will be
     */
    val params: MutableMap<String, String>,

    private val transactionRunner: ContentJobItemTransactionRunner?,

    override val di: DI
) : DIAware {

    private val downloadLock: Mutex by lazy { Mutex() }

    @Volatile
    private var downloadedTmpUri: DoorUri? = null

    /**
     * Similar to .use on an inputStream. This will run the block, and then delete any temporary
     * directories.
     */
    suspend fun <R> use(block: suspend (ContentJobProcessContext) -> R): R {
        return try {
            block(this)
        }finally {
            tempDirUri.emptyRecursively()
        }
    }

    /**
     * Concurrent updates to ContentJobItem can cause a transaction deadlock on postgres. Therefor
     * all updates to ContentJobItem need to be done in a Mutex (e.g. progress, etc).
     *
     * This function will use a Mutex and start a database transaction.
     *
     * WARNING: This is NOT a Reentrant lock.
     */
    suspend fun <R> withContentJobItemTransactionMutex(block: suspend (UmAppDatabase) -> R): R {
        return transactionRunner?.withContentJobItemTransaction(block)
            ?: throw IllegalStateException("withContentJobItemTransaction requires contentJobItemRunner")
    }

    suspend fun getLocalOrCachedUri(): DoorUri {
        if (!srcUri.isRemote()) {
            return srcUri
        }

        downloadLock.withLock {
            val downloadedTmpUriVal = downloadedTmpUri
            if(downloadedTmpUriVal != null)
                return downloadedTmpUriVal

            val downloadDestUri = DoorUri.parse(UMFileUtil.joinPaths(tempDirUri.toString(),
                    "tempFile"))

            srcUri.downloadUrlIfRemote(downloadDestUri, di)
            downloadedTmpUri = downloadDestUri
            return downloadDestUri
        }
    }

}