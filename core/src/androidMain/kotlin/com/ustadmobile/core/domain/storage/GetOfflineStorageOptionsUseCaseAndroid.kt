package com.ustadmobile.core.domain.storage

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import com.ustadmobile.core.MR

class GetOfflineStorageOptionsUseCaseAndroid(
    private val appContext: Context,
): GetOfflineStorageOptionsUseCase {

    private val internalStorage = OfflineStorageOption(
        label = MR.strings.phone_memory,
        path = appContext.filesDir.absolutePath
    )

    override fun invoke(): List<OfflineStorageOption> {
        val externalFilesDir = if(
            Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        ){
            ContextCompat.getExternalFilesDirs(appContext, null).firstOrNull()
        }else {
            null
        }

        return if(externalFilesDir != null) {
            listOf(
                internalStorage,
                OfflineStorageOption(
                    label = MR.strings.memory_card,
                    path = externalFilesDir.absolutePath,
                )
            )
        }else {
            listOf(internalStorage)
        }
    }
}