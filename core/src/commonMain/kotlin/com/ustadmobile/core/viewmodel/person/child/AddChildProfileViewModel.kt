package com.ustadmobile.core.viewmodel.person.child

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI



data class AddChildProfileUiState(
    val onAddChildProfile: String? = null,

)

class AddChildProfileViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(
        AddChildProfileUiState()
    )

    val uiState: Flow<AddChildProfileUiState> = _uiState.asStateFlow()


    init {
        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MR.strings.add_child_profiles),
                hideBottomNavigation = true,
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.finish),
                    onClick = this@AddChildProfileViewModel::onClickFinish
                )
            )
        }
    }

    fun onClickFinish() {

    }

    fun onClickAddChileProfile() {
        navController.navigate(EditChildProfileViewModel.DEST_NAME, emptyMap())

    }

    companion object {

        const val DEST_NAME = "AddChildProfile"

    }
}