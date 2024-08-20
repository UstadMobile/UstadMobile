package com.ustadmobile.core.viewmodel.person.child

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI


data class AddChildProfileUiState(
    val onAddChildProfile: String? = null,
    val childProfiles: List<Person> = emptyList(),
)

class AddChildProfileViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME) {

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
        viewModelScope.launch {
            val effectiveDb = activeRepo ?: activeDb
            val persons = effectiveDb.personDao()
                .getMinorByParentPersonUidAsync(accountManager.currentUserSession.person.personUid)
            persons?.let {
              _uiState.update { prev->
                  prev.copy(
                      childProfiles = it
                  )
              }
            }
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