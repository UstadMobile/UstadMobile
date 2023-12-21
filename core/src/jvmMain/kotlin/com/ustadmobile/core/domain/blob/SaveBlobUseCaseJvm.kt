package com.ustadmobile.core.domain.blob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
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
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.HttpPathResponse
import kotlinx.io.files.FileSystem
import kotlinx.io.files.SystemFileSystem

class SaveBlobUseCaseJvm(
    private val cache: UstadCache,
    private val uriHelper: UriHelper,
    private val tmpDir: Path,
    private val dbProvider: (Endpoint, Int) -> UmAppDatabase,
    private val adapterProvider: (tableId: Int) -> BlobEntityAdapter,
    private val fileSystem: FileSystem = SystemFileSystem,
) : SaveBlobUseCase{

    /**
     * First store the blobs in the cache as https://endpoint/api/sha256, then upload them
     */
    override suspend fun invoke(
        endpoint: Endpoint,
        tableId: Int,
        blobs: List<SaveBlobUseCase.BlobToSave>
    ) {
        val endpointUrl = UrlKmp(endpoint.url)
        val db: UmAppDatabase = dbProvider(endpoint, DoorTag.TAG_REPO)

        val adapter = adapterProvider(tableId)

        val entriesToStore = blobs.map { blob ->
            val tmpBlobPath = Path(tmpDir, UUID.randomUUID().toString())
            val localUri = blob.localUri
            if(localUri != null){
                val blobDoorUri = DoorUri.parse(blob.localUri)

                val transferResult = uriHelper.openSource(blobDoorUri)
                    .transferToAndGetSha256(tmpBlobPath)

                val sha256Base64 = transferResult.sha256.encodeBase64()

                val blobUrl = endpointUrl.resolve("/api/blob/" +
                        UMURLEncoder.encodeUTF8(sha256Base64))


                val blobUrlStr = blobUrl.toString()
                val blobRequest = requestBuilder(blobUrlStr) {  }

                blob.uid to CacheEntryToStore(
                    request = blobRequest,
                    response = HttpPathResponse(
                        path = tmpBlobPath,
                        fileSystem = fileSystem,
                        mimeType = uriHelper.getMimeType(blobDoorUri) ?: "application/octet-stream",
                        request = blobRequest,
                        extraHeaders = headersBuilder {
                            header("cache-control", "immutable")
                        },
                    )
                )
            }else {
                blob.uid to null
            }
        }

        cache.store(entriesToStore.mapNotNull { it.second })

        val updates = entriesToStore.map {
            BlobEntityAdapter.BlobUpdate(
                uid = it.first,
                uri = it.second?.request?.url
            )
        }

        adapter.updateBlobUri(db, updates)

        //After storing, attempt upload

        //After upload - replicate new url

    }
}