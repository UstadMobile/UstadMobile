package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI


data class HtmlEditUiState(
    val html: String = "",
    val wordLimit: Int? = null,
    val charLimit: Int? = null,
    val wordCount: Int? = null,
    val charCount: Int? = null,
)

class HtmlEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(HtmlEditUiState())

    val uiState: Flow<HtmlEditUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { prev ->
            prev.copy(
                html = savedStateHandle[ARG_HTML] ?: "",
                wordLimit = savedStateHandle[ARG_WORD_LIMIT]?.toInt() ?: 0,
                charLimit = savedStateHandle[ARG_CHAR_LIMIT]?.toInt() ?: 0,
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.done),
                    enabled = true,
                    onClick = this::onClickDone
                )
            )
        }
    }

    fun onHtmlChanged(html: String) {
        _uiState.update { prev ->
            prev.copy(html = html)
        }

        saveStateJob?.cancel()
        saveStateJob = viewModelScope.launch {
            delay(200)
            savedStateHandle[KEY_STATE_CURRENT_HTML] = html
        }
    }

    fun onClickDone() {
        finishWithResult(_uiState.value.html)
    }

    companion object {

        const val ARG_HTML = "html"

        const val ARG_WORD_LIMIT = "wordLimit"

        const val ARG_CHAR_LIMIT = "charLimit"

        const val DEST_NAME = "HtmlEdit"

        const val KEY_STATE_CURRENT_HTML = "currentHtml"
    }
}