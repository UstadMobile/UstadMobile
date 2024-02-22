package com.ustadmobile.core.viewmodel.clazz.joinwithcode

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class JoinWithCodeUiState(

    val codeError: String? = null,

    val code: String = "",

    val fieldsEnabled: Boolean = true,

)

class JoinWithCodeViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {


    private val _uiState = MutableStateFlow(JoinWithCodeUiState())

    val uiState: Flow<JoinWithCodeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {

        }
    }


    fun onCodeValueChange(codeVal: String) {
        _uiState.update { prev ->
            prev.copy(code = codeVal)
        }
    }

    fun onClickJoin() {

    }

    companion object {

        const val DEST_NAME = "JoinWithCode"

    }

}
