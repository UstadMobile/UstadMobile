package com.ustadmobile.core.domain.storage

import android.content.Context
import android.os.Environment
import com.ustadmobile.core.ext.isChildOf
import java.io.File

/**
 * Find the SD Card path (if any is available). Android's storage APIs make this far more
 * challenging than one would expect.
 *
 * As per:
 * https://developer.android.com/reference/android/os/Environment#isExternalStorageRemovable(java.io.File)
 *
 * "External" does not actually mean it is an SD card, USB stick, or what one would normally consider
 * external storage. It can be "emulated" on the device itself.
 */
class GetAndroidSdCardDirUseCase(private val appContext: Context) {

    data class SdCardDirs(
        val filesDir: File,
        val cacheDir: File,
    )

    operator fun invoke(): SdCardDirs? {
        val removableExternalFileDirs = appContext.getExternalFilesDirs(null).firstOrNull {
            Environment.isExternalStorageRemovable(it)
        }

        val removableExternalFileDirsParent = removableExternalFileDirs?.parentFile

        val removableCacheDir =if(removableExternalFileDirsParent != null) {
            appContext.externalCacheDirs.firstOrNull { it.isChildOf(removableExternalFileDirsParent) }
        }else {
            null
        }

        return if(
            Environment.getExternalStorageState(removableExternalFileDirs) == Environment.MEDIA_MOUNTED &&
            removableExternalFileDirs != null &&
            removableCacheDir != null
        ) {
            SdCardDirs(removableExternalFileDirs, removableCacheDir)
        }else {
            null
        }

    }
}