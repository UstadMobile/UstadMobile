package com.ustadmobile.core.domain.storage

interface GetOfflineStorageOptionsUseCase {

    operator fun invoke(): List<OfflineStorageOption>

    companion object {
        const val PREFKEY_OFFLINE_STORAGE = "offlineStoragePath"
    }

}