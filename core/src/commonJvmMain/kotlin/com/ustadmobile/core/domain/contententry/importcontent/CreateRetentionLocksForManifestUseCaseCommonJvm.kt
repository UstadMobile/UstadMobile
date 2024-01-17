package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.libcache.EntryLockRequest
import com.ustadmobile.libcache.UstadCache

class CreateRetentionLocksForManifestUseCaseCommonJvm(
    private val cache: UstadCache,
): CreateRetentionLocksForManifestUseCase {
    override suspend fun invoke(
        contentEntryVersionUid: Long,
        manifestUrl: String,
        manifest: ContentManifest,
    ): List<CreateRetentionLocksForManifestUseCase.ManifestRetentionLock> {
        val lockRequests = (manifest.entries.map { it.bodyDataUrl } + manifestUrl).map {
            EntryLockRequest(url = it)
        }

        return cache.addRetentionLocks(
            lockRequests
        ).map {
            CreateRetentionLocksForManifestUseCase.ManifestRetentionLock(
                url = it.first.url,
                lockId = it.second.lockId,
            )
        }
    }
}