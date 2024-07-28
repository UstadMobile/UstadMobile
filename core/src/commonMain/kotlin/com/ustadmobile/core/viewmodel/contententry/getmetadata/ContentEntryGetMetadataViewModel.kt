package com.ustadmobile.core.viewmodel.contententry.getmetadata

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetadataStatus
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetaDataFromUriUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.door.DoorUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import com.ustadmobile.core.MR
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.UnsupportedContentException
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditViewModel

data class ContentEntryGetMetadataUiState(
    val status: ContentEntryGetMetadataStatus = ContentEntryGetMetadataStatus(),
)

class ContentEntryGetMetadataViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    private val contentEntryGetMetaDataFromUriUseCase: ContentEntryGetMetaDataFromUriUseCase =
        di.onActiveEndpoint().direct.instance(),
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(ContentEntryGetMetadataUiState())

    val uiState: Flow<ContentEntryGetMetadataUiState> = _uiState.asStateFlow()

    init {
        val uriArg = savedStateHandle[ARG_URI] ?: throw IllegalArgumentException("No uri")
        val fileName = savedStateHandle[ARG_FILENAME]

        viewModelScope.launch {
            try {
                val metadataResult = contentEntryGetMetaDataFromUriUseCase(
                    contentUri = DoorUri.parse(uriArg),
                    endpoint = accountManager.activeEndpoint,
                    fileName = fileName,
                    onProgress = {
                        _uiState.update { prev ->
                            prev.copy(status = it)
                        }
                    }
                )

                navController.navigate(
                    viewName = ContentEntryEditViewModel.DEST_NAME,
                    args = buildMap {
                        put(
                            ContentEntryEditViewModel.ARG_IMPORTED_METADATA,
                            json.encodeToString(
                                serializer = MetadataResult.serializer(),
                                value = metadataResult
                            )
                        )
                        putFromSavedStateIfPresent(CourseBlockEditViewModel.COURSE_BLOCK_CONTENT_ENTRY_PASS_THROUGH_ARGS)
                        putFromSavedStateIfPresent(UstadView.ARG_RESULT_DEST_VIEWNAME)
                        putFromSavedStateIfPresent(UstadView.ARG_RESULT_DEST_KEY)
                        putFromSavedStateIfPresent(ARG_PARENT_UID)
                    },
                    goOptions = UstadMobileSystemCommon.UstadGoOptions(
                        popUpToViewName = DEST_NAME,
                        popUpToInclusive = true,
                    )
                )
            }catch(e: Throwable) {
                val errorMessage = when {
                    e is InvalidContentException -> {
                        "${systemImpl.getString(MR.strings.invalid_file)} : ${e.message}"
                    }
                    e is UnsupportedContentException -> {
                        systemImpl.formatString(MR.strings.unsupported_file_type, e.message ?:"")
                    }
                    else -> "${systemImpl.getString(MR.strings.error)} : other: ${e.message}"
                }
                _uiState.update { prev ->
                    prev.copy(
                        status = prev.status.copy(
                            error = errorMessage
                        )
                    )
                }
            }
        }
    }

    companion object {

        const val ARG_URI = "uri"

        const val ARG_FILENAME = "filename"

        const val DEST_NAME = "ContentEntryGetMetadata"

    }
}
