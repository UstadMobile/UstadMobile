package com.ustadmobile.core.viewmodel.person.bulkaddselectfile

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.blob.openblob.OpenBlobItem
import com.ustadmobile.core.domain.blob.openblob.OpenBlobUiUseCase
import com.ustadmobile.core.domain.blob.openblob.OpenBlobUseCase
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.person.bulkaddrunimport.BulkAddPersonRunImportViewModel
import kotlinx.coroutines.launch
import org.kodein.di.instance

data class BulkAddPersonSelectFileUiState(
    val selectedFileUri: String? = null,
    val selectedFileName: String? = null,
    val fileSelectError: String? = null,
    val fieldsEnabled: Boolean = false,
) {
    val importButtonEnabled: Boolean
        get() = selectedFileUri != null && fieldsEnabled
}

class BulkAddPersonSelectFileViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(
        BulkAddPersonSelectFileUiState()
    )

    val uiState: Flow<BulkAddPersonSelectFileUiState> = _uiState.asStateFlow()

    private val openBlobUiUseCase: OpenBlobUiUseCase by di.onActiveEndpoint().instance()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MR.strings.bulk_import),
            )
        }

        launchIfHasPermission(
            permissionCheck = { db ->
                db.systemPermissionDao().personHasSystemPermissionPair(
                    accountPersonUid = activeUserPersonUid,
                    firstPermission = PermissionFlags.ADD_PERSON,
                    secondPermission = PermissionFlags.PERSON_VIEW,
                ).let { it.firstPermission && it.secondPermission }
            },
            setLoadingState = true,
            onSetFieldsEnabled = {
                _uiState.update { prev -> prev.copy(fieldsEnabled = it) }
            }
        ) {
            //nothing more to do, fields will be enabled
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

    fun onClickGetTemplate() {
        val templatePath = accountManager.activeLearningSpace.url +
                "staticfiles/bulkaddpersons/bulk-add-persons-template.csv"

        viewModelScope.launch {
            try {
                openBlobUiUseCase(
                    openItem = OpenBlobItem(
                        uri = templatePath,
                        mimeType = "text/csv",
                        fileName = "bulk-add-persons-template.csv",
                        fileSize = 186,
                    ),
                    onUiUpdate =  {
                        //do nothing - its a tiny file
                    },
                    intent = OpenBlobUseCase.OpenBlobIntent.SEND, //On Android, send the file
                )
            }catch(e: Throwable) {
                e.printStackTrace()
            }
        }

    }

    companion object {

        const val DEST_NAME = "BulkAddPersonSelectFile"

    }
}