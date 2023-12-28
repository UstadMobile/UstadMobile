package com.ustadmobile.core.domain.blob.savelocaluris

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.url.UrlKmp
import com.ustadmobile.core.util.UMURLEncoder
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
            val mimeType = uriHelper.getMimeType(blobDoorUri) ?: "application/octet-stream"

            saveItem.entityUid to CacheEntryToStore(
                request = blobRequest,
                response = HttpPathResponse(
                    path = tmpBlobPath,
                    fileSystem = fileSystem,
                    mimeType = mimeType,
                    request = blobRequest,
                    extraHeaders = headersBuilder {
                        header("cache-control", "immutable")
                    },
                )
            )
        }

        Napier.d { "$logPrefix Storing ${entriesToStore.size} local uris as blobs (${entriesToStore.joinToString { it.second.request.url }})" }
        cache.store(entriesToStore.map { it.second })
        val uidToLocalUriMap = localUrisToSave.associate {
            it.entityUid to it.localUri
        }


        entriesToStore.map {
            //Blob local uri must be in the map
            val blobLocalUri = uidToLocalUriMap[it.first]!!
            SaveLocalUrisAsBlobsUseCase.SavedBlob(it.first, blobLocalUri, it.second.request.url)
        }
    }

}