package com.ustadmobile.core.viewmodel.contententry.subtitleedit

import com.ustadmobile.core.contentformats.media.SubtitleTrack
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState

data class SubtitleEditUiState(
    val entity: SubtitleTrack? = null,
    val titleError: String? = null,
    val fieldsEnabled: Boolean = false,
)

/**
 * Can use https://mvnrepository.com/artifact/fr.noop/subtitle
 * to validate
 */
class SubtitleEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadEditViewModel(
    di = di,
    savedStateHandle = savedStateHandle,
    destinationName = DEST_NAME,
) {

    private val supportedLanguagesConfig: SupportedLanguagesConfig by instance()

    private val _uiState = MutableStateFlow(SubtitleEditUiState())

    val uiState: Flow<SubtitleEditUiState> = _uiState.asSharedFlow()

    init {
        _appUiState.update {
            it.copy(
                title = systemImpl.getString(MR.strings.edit_subtitles),
                actionBarButtonState = ActionBarButtonUiState(
                    text = systemImpl.getString(MR.strings.done),
                    onClick = this::onClickDone
                )
            )
        }

        viewModelScope.launch {
            loadEntity(
                serializer = SubtitleTrack.serializer(),
                onLoadFromDb = { db ->
                    null
                },
                makeDefault = {
                    SubtitleTrack(
                        uri = savedStateHandle[ARG_URI] ?: "",
                        title = savedStateHandle[ARG_FILENAME] ?: "",
                        langCode = supportedLanguagesConfig.displayedLocale,
                        mimeType = "text/vtt"
                    )
                },
                uiUpdate = {
                    _uiState.update { prev -> prev.copy(entity = it) }
                }
            )

            _uiState.update { prev -> prev.copy(fieldsEnabled = true) }

            _appUiState.update {
                it.copy(
                    actionBarButtonState = it.actionBarButtonState.copy(visible = true)
                )
            }
        }
    }

    fun onEntityChanged(entity: SubtitleTrack?) {
        _uiState.update { prev ->
            prev.copy(entity = entity)
        }

        scheduleEntityCommitToSavedState(
            entity = entity,
            serializer = SubtitleTrack.serializer(),
            commitDelay = 200,
        )
    }

    fun onClickDone() {
        finishWithResult(_uiState.value.entity)
    }

    companion object {

        const val ARG_URI = "uri"

        const val ARG_FILENAME = "filename"

        const val DEST_NAME = "SubtitleEdit"

    }
}