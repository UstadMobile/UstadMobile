package com.ustadmobile.core.domain.localsharing.checkcontentavailability

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.libcache.UstadCache
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UstadCacheCheckContentAvailabilityUseCase(
    private val ustadCache: UstadCache,
    private val httpClient: HttpClient,
) : CheckContentAvailabilityUseCase {

    override suspend fun invoke(contentEntryVersion: ContentEntryVersion): Boolean {
        return try {
            val manifestUrl = contentEntryVersion.cevManifestUrl ?: throw IllegalArgumentException("null manifest url")
            val manifest: ContentManifest = httpClient.get(manifestUrl).body()
            val distinctUrls = manifest.entries.map { it.bodyDataUrl }.toSet()
            val availableUrls = withContext(Dispatchers.IO) {
                ustadCache.getEntriesLocallyAvailable(distinctUrls)
            }

            availableUrls.size == distinctUrls.size
        }catch(e: Throwable) {
            Napier.w("UstadCacheCheckContentAvailabilityUseCase : exception checking", e)
            false
        }
    }
}