package com.ustadmobile.core.domain.storage

import com.russhwolf.settings.Settings
import com.ustadmobile.core.domain.storage.GetOfflineStorageOptionsUseCase.Companion.PREFKEY_OFFLINE_STORAGE

class GetOfflineStorageSettingUseCase(
    private val getOfflineStorageOptionsUseCase: GetOfflineStorageOptionsUseCase,
    private val settings: Settings,
) {

    operator fun invoke(): OfflineStorageOption {
        val setting = settings.getStringOrNull(PREFKEY_OFFLINE_STORAGE)
        val options = getOfflineStorageOptionsUseCase()
        return options.takeIf { setting != null }?.firstOrNull {
            it.value == setting
        } ?: options.first()
    }
}