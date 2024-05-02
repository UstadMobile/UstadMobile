package com.ustadmobile.core.domain.storage

import com.ustadmobile.core.MR

class GetOfflineStorageOptionsUseCaseAndroid(
    private val getAndroidSdCardDirUseCase: GetAndroidSdCardDirUseCase,
): GetOfflineStorageOptionsUseCase {

    private val internalStorage = OfflineStorageOption(
        label = MR.strings.phone_memory,
        value = INTERNAL,
    )

    override fun invoke(): List<OfflineStorageOption> {
        return if(getAndroidSdCardDirUseCase() != null) {
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