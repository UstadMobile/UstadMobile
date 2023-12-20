package com.ustadmobile.core.domain.saveblob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.saveblob.adapters.PersonPictureAdapter
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
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class SaveBlobUseCaseJvm(
    private val cache: UstadCache,
    private val uriHelper: UriHelper,
    private val tmpDir: Path,
    private val di: DI,
    private val adapterProvider: (tableId: Int) -> BlobEntityAdapter,
    private val fileSystem: FileSystem = SystemFileSystem,
) : SaveBlobUseCase{

    override suspend fun invoke(endpoint: Endpoint, blobs: List<SaveBlobUseCase.BlobToSave>) {
        val endpointUrl = UrlKmp(endpoint.url)
        val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

        val adapter = PersonPictureAdapter()

        val entriesToStore = blobs.map { blob ->
            val tmpBlobPath = Path(tmpDir, UUID.randomUUID().toString())
            val blobDoorUri = DoorUri.parse(blob.localUri)
            val sha256 = uriHelper.openSource(blobDoorUri)
                .transferToAndGetSha256(tmpBlobPath)
            val sha256Base64 = sha256.sha256.encodeBase64()

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

        }

        cache.store(entriesToStore.map { it.second })

        val updates = entriesToStore.map {
            BlobEntityAdapter.BlobUpdate(
                uid = it.first,
                uri = it.second.request.url
            )
        }

        adapter.updateBlobUri(db, updates)

        //After storing, attempt upload

        //After

    }
}