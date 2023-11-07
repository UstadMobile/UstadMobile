package com.ustadmobile.core.viewmodel.pdfcontent

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class PdfContentUiState(
    val pdfUrl: String? = null,
)

class PdfContentViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val entityUidArg: Long = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private val _uiState = MutableStateFlow(PdfContentUiState())

    val uiState: Flow<PdfContentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val contentEntryVersion = activeRepo.contentEntryVersionDao
                .findByUidAsync(entityUidArg) ?: return@launch
            val cevUrl = contentEntryVersion.cevUrl ?: return@launch
            _uiState.update { prev ->
                prev.copy(pdfUrl = cevUrl)
            }

            val contentEntry = activeRepo.contentEntryDao.findByUidAsync(
                contentEntryVersion.cevContentEntryUid
            )
            _appUiState.update { prev ->
                prev.copy(title = contentEntry?.title ?: "")
            }
        }
    }

    companion object {

        const val DEST_NAME = "PdfContent"
    }
}