package com.ustadmobile.core.domain.storage

interface GetOfflineStorageAvailableSpace {

    suspend operator fun invoke(offlineStorageOption: OfflineStorageOption) : Long

}