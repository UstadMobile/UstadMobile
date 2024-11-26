package com.ustadmobile.core.viewmodel.contententry.getsubtitle

import com.ustadmobile.core.contentformats.media.SubtitleTrack
import com.ustadmobile.core.domain.contententry.getsubtitletrackfromuri.GetSubtitleTrackFromUriUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.require
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.contententry.subtitleedit.SubtitleEditViewModel
import com.ustadmobile.door.DoorUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.MR

data class GetSubtitleUiState(
    val error: String? = null,
)

/**
 * This works similarly to getmetadata - the subtitle URI is validated.
 *   On web: Uploads to server for validation
 *   On Android/Desktop: validation is done on device
 */
class GetSubtitleViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(GetSubtitleUiState())

    val uiState: Flow<GetSubtitleUiState> = _uiState.asStateFlow()

    private val getSubtitleTrackUseCase: GetSubtitleTrackFromUriUseCase by
        on(accountManager.activeEndpoint).instance()

    init {
        _appUiState.update {
            it.copy(title = systemImpl.getString(MR.strings.importing))
        }

        viewModelScope.launch {
            try {
                val subtitleTrack = getSubtitleTrackUseCase(
                    subtitleTrackUri = DoorUri.parse(savedStateHandle.require(SubtitleEditViewModel.ARG_URI)),
                    filename = savedStateHandle.require(SubtitleEditViewModel.ARG_FILENAME),
                )

                navController.navigate(
                    viewName = SubtitleEditViewModel.DEST_NAME,
                    args = buildMap {
                        put(
                            key = KEY_ENTITY_STATE,
                            value = json.encodeToString(SubtitleTrack.serializer(), subtitleTrack)
                        )

                        putFromSavedStateIfPresent(ARG_RESULT_DEST_VIEWNAME)
                        putFromSavedStateIfPresent(ARG_RESULT_DEST_KEY)
                    },
                    goOptions = UstadMobileSystemCommon.UstadGoOptions(
                        popUpToViewName = DEST_NAME,
                        popUpToInclusive = true,
                    ),
                )
            }catch(e: Throwable) {
                _uiState.update {
                    it.copy(
                        error = "${systemImpl.getString(MR.strings.error)}: ${e.message}",
                    )
                }
            }
        }
    }

    companion object {

        const val DEST_NAME = "GetSubtitle"
    }

}