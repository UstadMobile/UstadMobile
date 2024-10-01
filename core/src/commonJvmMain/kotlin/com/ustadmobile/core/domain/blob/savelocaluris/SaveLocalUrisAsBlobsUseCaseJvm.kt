package com.ustadmobile.core.domain.blob.savelocaluris

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.io.ext.toDoorUri
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.url.UrlKmp
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.core.util.digest.Digester
import com.ustadmobile.core.util.digest.urlKey
import com.ustadmobile.door.DoorUri
import com.ustadmobile.libcache.CacheEntryToStore
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.io.transferToAndGetSha256
import kotlinx.io.files.Path
import java.util.UUID
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.ihttp.headers.iHeadersBuilder
import com.ustadmobile.lib.db.entities.TransferJobItem
import com.ustadmobile.ihttp.request.iRequestBuilder
import com.ustadmobile.libcache.response.HttpPathResponse
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileSystem
import kotlinx.io.files.SystemFileSystem

/**
 * Note: When a local uri is saved as a blob, it will not be retained because we don't know if this
 * is just a temporary uri (e.g. a user previewing a picture) or something to keep.
 */
class SaveLocalUrisAsBlobsUseCaseJvm(
    private val learningSpace: LearningSpace,
    private val cache: UstadCache,
    private val uriHelper: UriHelper,
    private val tmpDir: Path,
    private val deleteUrisUseCase: DeleteUrisUseCase,
    private val fileSystem: FileSystem = SystemFileSystem,
) : SaveLocalUrisAsBlobsUseCase {

    private val logPrefix = "SaveLocalUrisAsBlobsUseCaseJvm"

    @Volatile
    private var tmpDirPathChecked: Boolean = false

    private fun createTmpPathIfNeeded() {
        if(!tmpDirPathChecked) {
            if(!fileSystem.exists(tmpDir)) {
                fileSystem.createDirectories(tmpDir)
            }
            tmpDirPathChecked = true
        }
    }

    private data class ProcessedEntry(
        val saveBlobItem: SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem,
        val cacheEntry: CacheEntryToStore,
        val cacheEntryTmpPath: Path,
    )

    /**
     * Store the blobs in the cache as https://endpoint/api/sha256
     */
    override suspend fun invoke(
        localUrisToSave: List<SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem>,
        onTransferJobItemCreated: (SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem, TransferJobItem) -> Unit,
    ): List<SaveLocalUrisAsBlobsUseCase.SavedBlob> = withContext(Dispatchers.Default) {
        createTmpPathIfNeeded()

        val learningSpaceUrl = UrlKmp(learningSpace.url)
        val digester = Digester("MD5")

        //List of Pair (SaveLocalUriAsBlobItem to CacheEntryToStore)
        val entriesToStore = localUrisToSave.map { saveItem ->
            val tmpBlobPath = Path(tmpDir, UUID.randomUUID().toString())

            val blobDoorUri = DoorUri.parse(saveItem.localUri)

            val transferResult = uriHelper.openSource(blobDoorUri)
                .transferToAndGetSha256(tmpBlobPath)

            val sha256Base64 = transferResult.sha256.encodeBase64()

            val blobUrl = learningSpaceUrl.resolve("api/blob/" +
                    UMURLEncoder.encodeUTF8(sha256Base64))

            val blobUrlStr = blobUrl.toString()
            val blobRequest = iRequestBuilder(blobUrlStr) {  }
            val mimeType = saveItem.mimeType ?: uriHelper.getMimeType(blobDoorUri)
                ?: "application/octet-stream"

            ProcessedEntry(
                saveBlobItem = saveItem,
                cacheEntry = CacheEntryToStore(
                    request = blobRequest,
                    response = HttpPathResponse(
                        path = tmpBlobPath,
                        fileSystem = fileSystem,
                        mimeType = mimeType,
                        request = blobRequest,
                        extraHeaders = iHeadersBuilder {
                            header("cache-control", "immutable")
                            saveItem.extraHeaders.names().forEach { extraHeaderName ->
                                saveItem.extraHeaders[extraHeaderName]?.also {
                                    header(extraHeaderName, it)
                                }
                            }
                        },
                    ),
                    createRetentionLock = saveItem.createRetentionLock,
                ),
                cacheEntryTmpPath = tmpBlobPath,
            )
        }

        Napier.d { "$logPrefix Storing ${entriesToStore.size} local uris as blobs " +
                "(${entriesToStore.joinToString { it.cacheEntry.request.url }})" }
        val storeResults = cache.store(
            entriesToStore.map { it.cacheEntry }
        ).associateBy {
            it.urlKey
        }

        val urisToDelete = entriesToStore.mapNotNull {
            if(it.saveBlobItem.deleteLocalUriAfterSave) it.saveBlobItem.localUri else null
        } + entriesToStore.map { it.cacheEntryTmpPath.toDoorUri().toString() }

        deleteUrisUseCase(urisToDelete)

        entriesToStore.map {
            val urlKey = digester.urlKey(it.cacheEntry.request.url)
            val cacheStoreResult = storeResults[urlKey]
                ?: throw IllegalStateException("Cache did not store ${it.cacheEntry.request.url}")

            SaveLocalUrisAsBlobsUseCase.SavedBlob(
                entityUid = it.saveBlobItem.entityUid,
                tableId = it.saveBlobItem.tableId,
                localUri = it.saveBlobItem.localUri,
                blobUrl = it.cacheEntry.request.url,
                retentionLockId = cacheStoreResult.lockId,
                integrity = cacheStoreResult.integrity,
                mimeType = it.cacheEntry.response.headers["content-type"] ?: "application/octet-stream",
                storageSize = cacheStoreResult.storageSize,
            )
        }
    }

}