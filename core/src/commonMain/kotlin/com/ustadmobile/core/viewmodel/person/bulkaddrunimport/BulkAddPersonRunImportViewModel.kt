package com.ustadmobile.core.viewmodel.person.bulkaddrunimport

import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonException
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsDataError
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsFromLocalUriUseCase
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.door.DoorUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

data class BulkAddPersonRunImportUiState(
    val inProgress: Boolean = true,
    val totalRecords: Int = 0,
    val numImported: Int = 0,
    val errors: List<BulkAddPersonsDataError> = emptyList(),
    val errorMessage: String? = null,
) {
    val hasErrors: Boolean
        get() = errors.isNotEmpty() || errorMessage != null
}

class BulkAddPersonRunImportViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(
        BulkAddPersonRunImportUiState()
    )

    val uiState: Flow<BulkAddPersonRunImportUiState>  =_uiState.asStateFlow()

    private val fileUri = savedStateHandle[ARG_URI] ?: throw IllegalArgumentException("No fileUri")

    private val bulkAddFromUriUseCase: BulkAddPersonsFromLocalUriUseCase by di.onActiveEndpoint()
        .instance()

    init {
        viewModelScope.launch {
            try {
                val result = bulkAddFromUriUseCase(DoorUri.parse(fileUri))
                _uiState.update { prev ->
                    prev.copy(
                        inProgress = false,
                        numImported = result.numImported
                    )
                }
            }catch(e: Throwable) {
                _uiState.update { prev ->
                    prev.copy(
                        inProgress = false,
                        errors = (e as? BulkAddPersonException)?.errors ?: emptyList(),
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    companion object {

        const val DEST_NAME = "BulkAddPersonRunImport"

        const val ARG_URI = "uri"

    }
}