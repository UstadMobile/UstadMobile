package com.ustadmobile.core.viewmodel.settings

import com.ustadmobile.core.domain.clipboard.SetClipboardStringUseCase
import com.ustadmobile.core.domain.getdeveloperinfo.GetDeveloperInfoUseCase
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import org.kodein.di.instance
import com.ustadmobile.core.MR

data class DeveloperSettingsUiState(
    val developerInfo: GetDeveloperInfoUseCase.DeveloperInfo = GetDeveloperInfoUseCase.DeveloperInfo(
        emptyMap()
    ),
)


class DeveloperSettingsViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle
): UstadViewModel(
    di, savedStateHandle, DEST_NAME,
) {

    private val getDevInfoUseCase: GetDeveloperInfoUseCase by instance()

    private val _uiState = MutableStateFlow(DeveloperSettingsUiState())

    val uiState: Flow<DeveloperSettingsUiState> = _uiState.asStateFlow()

    private val setClipboardStringUseCase: SetClipboardStringUseCase by instance()

    init {
        val devInfo = getDevInfoUseCase()
        _uiState.update { prev -> prev.copy(developerInfo = devInfo) }
        _appUiState.update { prev ->
            prev.copy(
                title = "Developer Settings",
                hideBottomNavigation = true,
            )
        }
    }

    fun onClickDeveloperInfo(entry: Map.Entry<String, String>) {
        setClipboardStringUseCase(entry.value)
        snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.copied_to_clipboard)))
    }


    companion object {

        const val DEST_NAME = "DevSettings"

        const val PREFKEY_DEVSETTINGS_ENABLED = "showDevSettings"

    }

}