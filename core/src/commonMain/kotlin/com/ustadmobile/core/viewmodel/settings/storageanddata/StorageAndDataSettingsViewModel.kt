package com.ustadmobile.core.viewmodel.settings.storageanddata

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.localsharing.setenabled.SetLocalSharingEnabledUseCase
import com.ustadmobile.core.domain.storage.GetOfflineStorageAvailableSpace
import com.ustadmobile.core.domain.storage.GetOfflineStorageOptionsUseCase
import com.ustadmobile.core.domain.storage.GetOfflineStorageSettingUseCase
import com.ustadmobile.core.domain.storage.OfflineStorageOption
import com.ustadmobile.core.domain.storage.SetOfflineStorageSettingUseCase
import com.ustadmobile.core.viewmodel.settings.SettingsOfflineStorageOption
import com.ustadmobile.core.viewmodel.settings.localsharing.LocalSharingSettingsViewModel
import kotlinx.coroutines.launch
import org.kodein.di.instance
import org.kodein.di.instanceOrNull

data class StorageAndSettingsUiState(
    val nearbySharingEnabled: Boolean = false,
    val localOnlyHotspotVisible: Boolean = false,
    val storageOptions: List<SettingsOfflineStorageOption> = emptyList(),
    val selectedOfflineStorageOption: OfflineStorageOption? = null,
    val storageOptionsDialogVisible: Boolean = false,
) {

    val offlineStorageOptionVisible: Boolean
        get() = storageOptions.isNotEmpty()

}

class StorageAndDataSettingsViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(StorageAndSettingsUiState())

    val uiState: Flow<StorageAndSettingsUiState> = _uiState.asStateFlow()

    private val getOfflineStorageSettingUseCase: GetOfflineStorageSettingUseCase? by instanceOrNull()

    private val setOfflineStorageSettingUseCase: SetOfflineStorageSettingUseCase? by instanceOrNull()

    private val getOfflineStorageAvailableSpace: GetOfflineStorageAvailableSpace? by instanceOrNull()

    private val getStorageOptionsUseCase: GetOfflineStorageOptionsUseCase? by instanceOrNull()

    private val setLocalSharingEnabledUseCase: SetLocalSharingEnabledUseCase? by instanceOrNull()

    private val settings: Settings by instance()

    init {
        _appUiState.update {
            it.copy(
                title = systemImpl.getString(MR.strings.storage_and_data),
                hideBottomNavigation = true,
            )
        }

        _uiState.update {
            it.copy(
                nearbySharingEnabled =
                    settings[SetLocalSharingEnabledUseCase.LOCAL_SHARING_ENABLED] ?: true
            )
        }

        viewModelScope.launch {
            val offlineStorageOptions = getStorageOptionsUseCase?.invoke()
            val selectedOfflineStorage = getOfflineStorageSettingUseCase?.invoke()
            if(offlineStorageOptions != null) {
                val optionsWithSpace = offlineStorageOptions.map {
                    SettingsOfflineStorageOption(
                        option = it,
                        availableSpace = getOfflineStorageAvailableSpace?.invoke(it) ?: 0
                    )
                }

                _uiState.update {
                    it.copy(
                        storageOptions = optionsWithSpace,
                        selectedOfflineStorageOption = selectedOfflineStorage ?: offlineStorageOptions.first(),
                    )
                }
            }
        }
    }

    fun onSetLocalSharingEnabled(enabled: Boolean) {
        setLocalSharingEnabledUseCase?.invoke(enabled)

        _uiState.update {
            it.copy(nearbySharingEnabled = enabled)
        }
    }

    fun onClickLocalSharing() {
        navController.navigate(LocalSharingSettingsViewModel.DEST_NAME, emptyMap())
    }

    fun onClickOfflineStorageOptionsDialog() {
        _uiState.update {
            it.copy(storageOptionsDialogVisible = true)
        }
    }

    fun onDismissOfflineStorageOptionsDialog() {
        _uiState.update {
            it.copy(storageOptionsDialogVisible = false)
        }
    }

    fun onSelectOfflineStorageOption(option: OfflineStorageOption) {
        onDismissOfflineStorageOptionsDialog()
        setOfflineStorageSettingUseCase?.invoke(option)
        _uiState.update {
            it.copy(
                selectedOfflineStorageOption = getOfflineStorageSettingUseCase?.invoke()
            )
        }
    }

    companion object {

        const val DEST_NAME = "StorageAndDataSettings"

    }
}