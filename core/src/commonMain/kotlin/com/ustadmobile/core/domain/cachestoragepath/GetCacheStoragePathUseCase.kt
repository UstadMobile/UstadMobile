package com.ustadmobile.core.domain.cachestoragepath

/**
 * Some components for Android and JVM require access to a disk file to display content (e.g. PDF).
 * They might come with their own support for loading from a URL, but this wouldn't enable
 * accessing the file offline because it wouldn't go through the system OKHttpClient. It would also
 * be inefficient, because there is already a copy of the file kept by lib-cache.
 *
 */
interface GetCacheStoragePathUseCase {

    operator fun invoke(url: String): String?

}