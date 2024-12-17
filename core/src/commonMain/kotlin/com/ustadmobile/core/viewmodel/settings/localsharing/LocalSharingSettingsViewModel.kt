package com.ustadmobile.core.viewmodel.settings.localsharing

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.localsharing.listneighbors.ListLocalSharingNeighborsUseCase
import kotlinx.coroutines.launch
import org.kodein.di.instance

data class LocalSharingSettingsUiState(
    val neighbors: List<ListLocalSharingNeighborsUseCase.LocalSharingNeighbor> = emptyList(),
)

class LocalSharingSettingsViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(LocalSharingSettingsUiState())

    val uiState: Flow<LocalSharingSettingsUiState> = _uiState.asStateFlow()

    private val listLocalSharingNeighborsUseCase: ListLocalSharingNeighborsUseCase by instance()

    init {
        _appUiState.update {
            it.copy(
                title = systemImpl.getString(MR.strings.local_sharing),
                hideBottomNavigation = true,
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


    companion object {

        const val DEST_NAME = "LocalSharingSettings"
    }
}