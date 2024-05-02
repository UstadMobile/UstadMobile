package com.ustadmobile.core.domain.storage

import android.content.Context
import android.os.Environment
import com.ustadmobile.libcache.CachePaths
import com.ustadmobile.libcache.CachePathsProvider
import com.ustadmobile.libcache.UstadCacheBuilder
import kotlinx.io.files.Path
import java.io.File


class CachePathsProviderAndroid(
    private val appContext: Context,
    private val getOfflineStorageSettingUseCase: GetOfflineStorageSettingUseCase,
): CachePathsProvider {

    override fun invoke(): CachePaths {
        val setting = getOfflineStorageSettingUseCase()
        val (externalFilesDir, externalCacheDir) = if(
            setting.value == GetOfflineStorageOptionsUseCaseAndroid.EXTERNAL &&
            Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        ) {
            Pair(appContext.getExternalFilesDir(null), appContext.externalCacheDir)
        }else {
            Pair(null, null)
        }

        return if(externalFilesDir != null && externalCacheDir != null) {
            CachePaths(
                tmpWorkPath = Path(externalFilesDir.absolutePath, UstadCacheBuilder.DEFAULT_SUBPATH_WORK),
                persistentPath = Path(externalFilesDir.absolutePath, UstadCacheBuilder.DEFAULT_SUBPATH_PERSISTENT),
                cachePath = Path(externalCacheDir.absolutePath, UstadCacheBuilder.DEFAULT_SUBPATH_CACHE),
            )
        }else {
            val basePath = File(appContext.filesDir, "httpfiles")
            CachePaths(
                tmpWorkPath = Path(basePath.absolutePath, UstadCacheBuilder.DEFAULT_SUBPATH_WORK),
                persistentPath = Path(basePath.absolutePath, UstadCacheBuilder.DEFAULT_SUBPATH_PERSISTENT),
                cachePath = Path(appContext.cacheDir.absolutePath, UstadCacheBuilder.DEFAULT_SUBPATH_CACHE),
            )
        }
    }
}