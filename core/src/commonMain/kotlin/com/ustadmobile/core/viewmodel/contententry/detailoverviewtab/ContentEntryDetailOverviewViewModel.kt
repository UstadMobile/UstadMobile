package com.ustadmobile.core.viewmodel.contententry.detailoverviewtab

import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentViewModel
import com.ustadmobile.core.viewmodel.pdfcontent.PdfContentViewModel
import com.ustadmobile.core.viewmodel.videocontent.VideoContentViewModel
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentViewModel
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ContentEntryDetailOverviewUiState(

    val scoreProgress: ContentEntryStatementScoreProgress? = null,

    val contentEntry: ContentEntryWithMostRecentContainer? = null,

    val contentEntryButtons: ContentEntryButtonModel? = null,

    val locallyAvailable: Boolean = false,

    val markCompleteVisible: Boolean = false,

    val translationVisibile: Boolean = false,

    val availableTranslations: List<ContentEntryRelatedEntryJoinWithLanguage> = emptyList(),

    val activeContentJobItems: List<ContentJobItemProgress> = emptyList()

) {
    val scoreProgressVisible: Boolean
        get() = scoreProgress?.progress != null
                && scoreProgress.progress > 0

    val authorVisible: Boolean
        get() = !contentEntry?.author.isNullOrBlank()

    val publisherVisible: Boolean
        get() = !contentEntry?.publisher.isNullOrBlank()

    val licenseNameVisible: Boolean
        get() = !contentEntry?.licenseName.isNullOrBlank()

    val fileSizeVisible: Boolean
        get() = contentEntry?.container?.fileSize != null
                && contentEntry.container?.fileSize != 0L

    val scoreResultVisible: Boolean
        get() = scoreProgress != null

    val openButtonVisible: Boolean
        get() = true

}

class ContentEntryDetailOverviewViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : DetailViewModel<ContentEntry> (di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(ContentEntryDetailOverviewUiState())

    val uiState: Flow<ContentEntryDetailOverviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepo.contentEntryDao.findEntryWithContainerByEntryIdLive(
                        entryUuid = entityUidArg
                    ).collect {
                        _uiState.update { prev ->
                            prev.copy(
                                contentEntry = it
                            )
                        }

                        _appUiState.update { prev ->
                            prev.copy(
                                title = it?.title ?: ""
                            )
                        }
                    }
                }

                launch {
                    activeRepo.scopedGrantDao.userHasSystemLevelPermissionAsFlow(
                        accountManager.currentAccount.personUid, Role.PERMISSION_CONTENT_INSERT
                    ).collect { hasPermission ->
                        _appUiState.update { prev ->
                            if(prev.fabState.visible != hasPermission) {
                                prev.copy(
                                    fabState = FabUiState(
                                        visible =  hasPermission
                                    )
                                )
                            }else {
                                prev
                            }
                        }
                    }
                }
            }
        }
    }

    fun onClickEdit() {

    }

    fun onClickOpen() {
        viewModelScope.launch {
            val latestContentEntryVersion = activeRepo.contentEntryVersionDao
                .findLatestVersionUidByContentEntryUidEntity(entityUidArg)
            if(latestContentEntryVersion != null) {
                val destName = when(latestContentEntryVersion.cevContentType) {
                    ContentEntryVersion.TYPE_XAPI -> XapiContentViewModel.DEST_NAME
                    ContentEntryVersion.TYPE_PDF -> PdfContentViewModel.DEST_NAME
                    ContentEntryVersion.TYPE_EPUB -> EpubContentViewModel.DEST_NAME
                    ContentEntryVersion.TYPE_VIDEO -> VideoContentViewModel.DEST_NAME
                    else -> null
                }

                if(destName != null) {
                    navController.navigate(
                        destName,
                        args = mapOf(
                            ARG_ENTITY_UID to latestContentEntryVersion.cevUid.toString()
                        )
                    )
                }
            }
        }
    }

    companion object {

        const val DEST_NAME = "ContentEntryDetailOverviewView"

    }

}