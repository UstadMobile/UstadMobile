package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.contentformats.manifest.ContentManifest

/**
 * This UseCase will create cache retention locks for all urls that are referenced by the given
 * manifest and the manifest url itself (see lib-cache for info on the retention lock concept).
 *
 * This is used on Android and Desktop clients when content is added and the retention locks must
 * be in place until the upload is complete, and on the server to ensure that all urls referenced
 * by the latest ContentEntryVersion manifest are retained.
 */
interface CreateRetentionLocksForManifestUseCase {

    data class ManifestRetentionLock(
        val url: String,
        val lockId: Long,
    )

    suspend operator fun invoke(
        contentEntryVersionUid: Long,
        manifestUrl: String,
        manifest: ContentManifest,
    ): List<ManifestRetentionLock>

}