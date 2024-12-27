package com.ustadmobile.core.viewmodel.settings.localsharing

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
import com.ustadmobile.core.domain.localsharing.devicename.GetLocalSharingDeviceNameUseCase
import com.ustadmobile.core.domain.localsharing.listneighbors.ListLocalSharingNeighborsUseCase
import com.ustadmobile.core.domain.localsharing.setenabled.SetLocalSharingEnabledUseCase
import kotlinx.coroutines.launch
import org.kodein.di.instance
import org.kodein.di.instanceOrNull

data class LocalSharingSettingsUiState(
    val neighbors: List<ListLocalSharingNeighborsUseCase.LocalSharingNeighbor> = emptyList(),
    val deviceName: String = "",
    val enabled: Boolean = false,
)

class LocalSharingSettingsViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(LocalSharingSettingsUiState())

    val uiState: Flow<LocalSharingSettingsUiState> = _uiState.asStateFlow()

    private val listLocalSharingNeighborsUseCase: ListLocalSharingNeighborsUseCase by instance()

    private val settings: Settings by instance()

    private val setLocalSharingEnabledUseCase: SetLocalSharingEnabledUseCase? by instanceOrNull()

    private val getDeviceNameUseCase: GetLocalSharingDeviceNameUseCase? by instanceOrNull()

    init {
        _appUiState.update {
            it.copy(
                title = systemImpl.getString(MR.strings.local_sharing),
                hideBottomNavigation = true,
            )
        }

        _uiState.update {
            it.copy(
                enabled = settings[SetLocalSharingEnabledUseCase.LOCAL_SHARING_ENABLED] ?: true,
                deviceName = getDeviceNameUseCase?.invoke() ?: ""
            )
        }

        viewModelScope.launch {
            listLocalSharingNeighborsUseCase().collect {
                _uiState.update { uiState ->
                    uiState.copy(
                        neighbors = it,
                    )
                }
            }
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        setLocalSharingEnabledUseCase?.invoke(enabled)

        _uiState.update {
            it.copy(enabled = enabled)
        }
    }


    companion object {

        const val DEST_NAME = "LocalSharingSettings"
    }
}