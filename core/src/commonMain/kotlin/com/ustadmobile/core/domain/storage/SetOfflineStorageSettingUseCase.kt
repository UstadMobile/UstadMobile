package com.ustadmobile.core.domain.storage

import com.russhwolf.settings.Settings
import com.ustadmobile.core.domain.storage.GetOfflineStorageOptionsUseCase.Companion.PREFKEY_OFFLINE_STORAGE

class SetOfflineStorageSettingUseCase(
    private val settings: Settings,
) {

    operator fun invoke(option: OfflineStorageOption) {
        settings.putString(PREFKEY_OFFLINE_STORAGE, option.value)
    }
}