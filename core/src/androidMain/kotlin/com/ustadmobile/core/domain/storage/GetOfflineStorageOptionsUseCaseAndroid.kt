package com.ustadmobile.core.domain.storage

import android.os.Environment
import com.ustadmobile.core.MR

class GetOfflineStorageOptionsUseCaseAndroid: GetOfflineStorageOptionsUseCase {

    private val internalStorage = OfflineStorageOption(
        label = MR.strings.phone_memory,
        value = INTERNAL,
    )

    override fun invoke(): List<OfflineStorageOption> {
        return if(
            Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        ) {
            listOf(
                internalStorage,
                OfflineStorageOption(MR.strings.memory_card, EXTERNAL),
            )
        }else {
            listOf(internalStorage)
        }
    }

    companion object {

        const val INTERNAL = "internal"

        const val EXTERNAL = "external"

    }
}