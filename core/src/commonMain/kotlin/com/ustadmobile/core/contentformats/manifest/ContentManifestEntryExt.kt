package com.ustadmobile.core.contentformats.manifest

import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase

/**
 * Return the total storage size that will be used by the given list of BlobAndContentManifestEntry
 */
fun List<SaveLocalUriAsBlobAndManifestUseCase.BlobAndContentManifestEntry>.totalStorageSize(): Long {
    return distinctBy { it.savedBlob.integrity }.sumOf {
        it.savedBlob.storageSize
    }
}
