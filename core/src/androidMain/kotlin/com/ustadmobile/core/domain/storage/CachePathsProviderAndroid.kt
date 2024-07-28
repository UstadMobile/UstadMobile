package com.ustadmobile.core.domain.storage

import android.content.Context
import com.ustadmobile.libcache.CachePaths
import com.ustadmobile.libcache.CachePathsProvider
import com.ustadmobile.libcache.UstadCacheBuilder
import kotlinx.io.files.Path
import java.io.File


class CachePathsProviderAndroid(
    private val appContext: Context,
    private val getAndroidSdCardPathUseCase: GetAndroidSdCardDirUseCase,
    private val getOfflineStorageSettingUseCase: GetOfflineStorageSettingUseCase,
): CachePathsProvider {

    override fun invoke(): CachePaths {
        val setting = getOfflineStorageSettingUseCase()
        val externalCachePaths = getAndroidSdCardPathUseCase
            .takeIf { setting.value == GetOfflineStorageOptionsUseCaseAndroid.EXTERNAL }
            ?.invoke()?.let {
                CachePaths(
                    tmpWorkPath = Path(it.filesDir.absolutePath, UstadCacheBuilder.DEFAULT_SUBPATH_WORK),
                    persistentPath = Path(it.filesDir.absolutePath, UstadCacheBuilder.DEFAULT_SUBPATH_PERSISTENT),
                    cachePath = Path(it.cacheDir.absolutePath, UstadCacheBuilder.DEFAULT_SUBPATH_CACHE),
                )
            }

        val basePath = File(appContext.filesDir, "httpfiles")
        return externalCachePaths ?: CachePaths(
            tmpWorkPath = Path(basePath.absolutePath, UstadCacheBuilder.DEFAULT_SUBPATH_WORK),
            persistentPath = Path(basePath.absolutePath, UstadCacheBuilder.DEFAULT_SUBPATH_PERSISTENT),
            cachePath = Path(appContext.cacheDir.absolutePath, UstadCacheBuilder.DEFAULT_SUBPATH_CACHE),
        )
    }
}