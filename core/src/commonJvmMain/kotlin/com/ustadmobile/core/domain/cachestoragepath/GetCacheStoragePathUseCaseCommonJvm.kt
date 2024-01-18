package com.ustadmobile.core.domain.cachestoragepath

import com.ustadmobile.libcache.UstadCache

class GetCacheStoragePathUseCaseCommonJvm(
    private val cache: UstadCache
): GetCacheStoragePathUseCase {
    override operator fun invoke(url: String): String? {
        return cache.getCacheEntry(url)?.storageUri
    }

}