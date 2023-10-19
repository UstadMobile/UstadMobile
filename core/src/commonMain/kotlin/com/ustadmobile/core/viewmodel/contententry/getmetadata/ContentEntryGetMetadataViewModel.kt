package com.ustadmobile.core.viewmodel.contententry.getmetadata

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetadataStatus
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.IContentEntryGetMetaDataFromUriUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
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

data class ContentEntryGetMetadataUiState(
    val status: ContentEntryGetMetadataStatus = ContentEntryGetMetadataStatus(),
)

class ContentEntryGetMetadataViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    private val contentEntryGetMetaDataFromUriUseCase: IContentEntryGetMetaDataFromUriUseCase = di.direct.instance(),
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(ContentEntryGetMetadataUiState())

    val uiState: Flow<ContentEntryGetMetadataUiState> = _uiState.asStateFlow()

    init {
        val uriArg = savedStateHandle[ARG_URI] ?: throw IllegalArgumentException("No uri")

        viewModelScope.launch {
            val metadataResult = contentEntryGetMetaDataFromUriUseCase(
                contentUri = DoorUri.parse(uriArg),
                endpoint = accountManager.activeEndpoint,
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
                },
                goOptions = UstadMobileSystemCommon.UstadGoOptions(
                    popUpToViewName = DEST_NAME,
                    popUpToInclusive = true,
                )
            )
        }

    }

    companion object {

        const val ARG_URI = "uri"

        const val DEST_NAME = "ContentEntryGetMetadata"

    }
}
