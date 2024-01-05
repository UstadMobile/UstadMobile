package com.ustadmobile.core.domain.blob.savelocaluris

import com.ustadmobile.core.account.Endpoint
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
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.HttpPathResponse
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileSystem
import kotlinx.io.files.SystemFileSystem

class SaveLocalUrisAsBlobsUseCaseJvm(
    private val endpoint: Endpoint,
    private val cache: UstadCache,
    private val uriHelper: UriHelper,
    private val tmpDir: Path,
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

    /**
     * First store the blobs in the cache as https://endpoint/api/sha256, then upload them
     *
     * Note: this will be tied to the database for progress update / status tracking purposes.
     */
    override suspend fun invoke(
        localUrisToSave: List<SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem>,
    ): List<SaveLocalUrisAsBlobsUseCase.SavedBlob> = withContext(Dispatchers.Default) {
        createTmpPathIfNeeded()

        val endpointUrl = UrlKmp(endpoint.url)
        val digester = Digester("MD5")

        //List of Pair (SaveLocalUriAsBlobItem to CacheEntryToStore)
        val entriesToStore = localUrisToSave.map { saveItem ->
            val tmpBlobPath = Path(tmpDir, UUID.randomUUID().toString())

            val blobDoorUri = DoorUri.parse(saveItem.localUri)

            val transferResult = uriHelper.openSource(blobDoorUri)
                .transferToAndGetSha256(tmpBlobPath)

            val sha256Base64 = transferResult.sha256.encodeBase64()

            val blobUrl = endpointUrl.resolve("/api/blob/" +
                    UMURLEncoder.encodeUTF8(sha256Base64))

            val blobUrlStr = blobUrl.toString()
            val blobRequest = requestBuilder(blobUrlStr) {  }
            val mimeType = saveItem.mimeType ?: uriHelper.getMimeType(blobDoorUri)
                ?: "application/octet-stream"

            saveItem to CacheEntryToStore(
                request = blobRequest,
                response = HttpPathResponse(
                    path = tmpBlobPath,
                    fileSystem = fileSystem,
                    mimeType = mimeType,
                    request = blobRequest,
                    extraHeaders = headersBuilder {
                        header("cache-control", "immutable")
                    },
                ),
                createRetentionLock = true,
            )
        }

        Napier.d { "$logPrefix Storing ${entriesToStore.size} local uris as blobs (${entriesToStore.joinToString { it.second.request.url }})" }
        val storeResults = cache.store(entriesToStore.map { it.second }).associateBy {
            it.urlKey
        }

        entriesToStore.map {
            SaveLocalUrisAsBlobsUseCase.SavedBlob(
                entityUid = it.first.entityUid,
                localUri = it.first.localUri,
                blobUrl = it.second.request.url,
                retentionLockId = storeResults[digester.urlKey(it.second.request.url)]?.lockId ?: 0
            )
        }
    }

}