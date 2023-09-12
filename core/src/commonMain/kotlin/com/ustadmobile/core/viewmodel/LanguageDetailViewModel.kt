package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.LanguageDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class LanguageDetailUiState(
    val language: Language? = null
)

class LanguageDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): DetailViewModel<Language>(di, savedStateHandle, LanguageDetailView.VIEW_NAME) {

    private val _uiState = MutableStateFlow(LanguageDetailUiState())

    val uiState: Flow<LanguageDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    if (entityUidArg != null) {
                        activeDb.languageDao.findByUidAsFlow(entityUidArg).collect { lang ->
                            _uiState.update { prev ->
                                prev.copy(
                                    language = lang
                                )
                            }
                            _appUiState.update { prev ->
                                prev.copy(
                                    title = lang?.name ?: "",
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}