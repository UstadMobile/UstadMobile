package com.ustadmobile.core.viewmodel.appearance

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class AppearanceDetailUiState(
    val organisationName: String? = null,
    val organisationLogo: String? = null,
    val jetpackComposeTheme: String? = null,
    val muiTheme: String? = null
)

class AppearanceDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String = DEST_NAME
) : UstadEditViewModel(di, savedStateHandle, destName) {

    private val _uiState: MutableStateFlow<AppearanceDetailUiState> = MutableStateFlow(AppearanceDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadingState = LoadingUiState.NOT_LOADING

        _appUiState.update {
            AppUiState(
                title =systemImpl.getString(MR.strings.appearance),
                hideBottomNavigation = false
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text =systemImpl.getString(MR.strings.edit),
                    onClick = this@AppearanceDetailViewModel::onClickEdit
                )
            )
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    organisationName = systemImpl.getString(MR.strings.app_name),
                    jetpackComposeTheme = systemImpl.getString(MR.strings.jetpack_compose_theme_name),
                    muiTheme = systemImpl.getString(MR.strings.mui_theme_name),
                )
            }
        }
    }


    fun onClickEdit() {
        navController.navigate(AppearanceEditViewModel.DEST_NAME, emptyMap())
    }

    companion object {
        const val DEST_NAME = "AppearanceDetailView"
    }
}