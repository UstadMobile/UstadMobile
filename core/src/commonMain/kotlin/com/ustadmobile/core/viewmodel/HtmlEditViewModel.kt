package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.countWords
import com.ustadmobile.core.util.ext.htmlToPlainText
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

    private val argDoneButtonLabel = savedStateHandle[ARG_DONE_STR]

    private val argTitle = savedStateHandle[ARG_TITLE]

    init {
        val htmlText = savedStateHandle[ARG_HTML] ?: ""
        val htmlPlainText by lazy {
            htmlText.htmlToPlainText()
        }
        _uiState.update { prev ->
            prev.copy(
                html = htmlText,
                wordLimit = savedStateHandle[ARG_WORD_LIMIT]?.toInt(),
                charLimit = savedStateHandle[ARG_CHAR_LIMIT]?.toInt(),
                wordCount = if(savedStateHandle[ARG_WORD_LIMIT] != null) {
                    htmlPlainText.countWords()
                }else {
                    null
                },
                charCount = if(savedStateHandle[ARG_CHAR_LIMIT] != null) {
                    htmlPlainText.length
                }else {
                    null
                }
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
                title = argTitle,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = argDoneButtonLabel ?: systemImpl.getString(MessageID.done),
                    enabled = true,
                    onClick = this::onClickDone
                )
            )
        }
    }

    fun onHtmlChanged(html: String) {
        val htmlPlainText by lazy {
            html.htmlToPlainText()
        }

        _uiState.update { prev ->
            prev.copy(
                html = html,
                wordCount = prev.wordLimit?.let { htmlPlainText.countWords() },
                charCount = prev.charLimit?.let { htmlPlainText.length }
            )
        }

        saveStateJob?.cancel()
        saveStateJob = viewModelScope.launch {
            delay(200)
            savedStateHandle[KEY_STATE_CURRENT_HTML] = html
        }
    }

    fun onClickDone() {
        val plainText by lazy { _uiState.value.html.htmlToPlainText() }
        if(
            _uiState.value.wordLimit?.let { it >= plainText.countWords() } == false ||
                _uiState.value.charLimit?.let { it >= plainText.length } == false
        ) {
            snackDispatcher.showSnackBar(Snack(message = systemImpl.getString(MessageID.error_too_long_text)))
            return
        }

        finishWithResult(_uiState.value.html)
    }

    companion object {

        const val ARG_HTML = "html"

        const val ARG_WORD_LIMIT = "wordLimit"

        const val ARG_CHAR_LIMIT = "charLimit"

        const val ARG_TITLE = "title"

        const val ARG_DONE_STR = "done"

        const val DEST_NAME = "HtmlEdit"

        const val KEY_STATE_CURRENT_HTML = "currentHtml"
    }
}