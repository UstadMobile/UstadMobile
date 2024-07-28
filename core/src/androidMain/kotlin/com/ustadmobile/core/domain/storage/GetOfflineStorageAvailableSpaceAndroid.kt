package com.ustadmobile.core.domain.storage

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetOfflineStorageAvailableSpaceAndroid(
    private val getAndroidSdCardDirUseCase: GetAndroidSdCardDirUseCase,
    private val appContext: Context,
): GetOfflineStorageAvailableSpace {

    override suspend fun invoke(offlineStorageOption: OfflineStorageOption): Long {
        return withContext(Dispatchers.IO) {
            if(offlineStorageOption.value == GetOfflineStorageOptionsUseCaseAndroid.EXTERNAL) {
                getAndroidSdCardDirUseCase()?.filesDir?.freeSpace ?: -1
            }else {
                appContext.filesDir.freeSpace
            }
        }
    }
}