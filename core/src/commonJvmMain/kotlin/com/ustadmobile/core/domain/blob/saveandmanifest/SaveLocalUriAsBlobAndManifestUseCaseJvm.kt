package com.ustadmobile.core.domain.blob.saveandmanifest

import com.ustadmobile.core.contentformats.manifest.ContentManifestEntry
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.fileExtensionOrNull
import com.ustadmobile.core.util.stringvalues.stringValuesOf
import com.ustadmobile.libcache.headers.MimeTypeHelper

class SaveLocalUriAsBlobAndManifestUseCaseJvm(
    private val saveLocalUrisAsBlobsUseCase: SaveLocalUrisAsBlobsUseCase,
    private val mimeTypeHelper: MimeTypeHelper,
) : SaveLocalUriAsBlobAndManifestUseCase {

    override suspend fun invoke(
        items: List<SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem>)
    : List<SaveLocalUriAsBlobAndManifestUseCase.BlobAndContentManifestEntry> {
        val savedBlobs = saveLocalUrisAsBlobsUseCase(
            items.map { it.blobItem }
        ).associateBy { it.localUri }

        return items.map { item ->
            val blobForUri = savedBlobs[item.blobItem.localUri]
                ?: throw IllegalStateException("SaveLocalUriAsBlobAndManifestUseCase: SaveBlob failed for ${item.blobItem.localUri}")
            val mimeType = item.manifestMimeType ?: item.manifestUri.fileExtensionOrNull()?.let {
                mimeTypeHelper.guessByExtension(it)
            } ?: blobForUri.mimeType

            val manifestEntry = ContentManifestEntry(
                uri = item.manifestUri,
                storageSize = blobForUri.storageSize,
                bodyDataUrl = blobForUri.blobUrl,
                responseHeaders = stringValuesOf(
                    "content-type" to listOf(mimeType),
                    "etag" to listOf(blobForUri.integrity),
                ),
                integrity = blobForUri.integrity
            )

            SaveLocalUriAsBlobAndManifestUseCase.BlobAndContentManifestEntry(
                savedBlob = blobForUri,
                manifestEntry = manifestEntry
            )
        }
    }
}