package com.ustadmobile.core.viewmodel.person.bulkaddselectfile

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.person.bulkaddrunimport.BulkAddPersonRunImportViewModel

data class BulkAddPersonSelectFileUiState(
    val selectedFileUri: String? = null,
    val selectedFileName: String? = null,
    val fileSelectError: String? = null,
) {
    val importButtonEnabled: Boolean
        get() = selectedFileUri != null
}

class BulkAddPersonSelectFileViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(
        BulkAddPersonSelectFileUiState()
    )

    val uiState: Flow<BulkAddPersonSelectFileUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MR.strings.bulk_import),
            )
        }
    }

    fun onFileSelected(uri: String, name: String) {
        _uiState.update { prev ->
            prev.copy(
                selectedFileUri = uri,
                selectedFileName = name,
            )
        }
    }

    fun onClickImportButton() {
        navController.navigate(
            BulkAddPersonRunImportViewModel.DEST_NAME,
            mapOf(BulkAddPersonRunImportViewModel.ARG_URI to (_uiState.value.selectedFileUri ?: ""))
        )
    }

    companion object {

        const val DEST_NAME = "BulkAddPersonSelectFile"

    }
}