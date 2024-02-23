package com.ustadmobile.core.viewmodel.contententry.detailoverviewtab

import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.blob.download.CancelDownloadUseCase
import com.ustadmobile.core.domain.blob.download.MakeContentEntryAvailableOfflineUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.LaunchContentEntryVersionUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.epub.LaunchEpubUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.xapi.LaunchXapiUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.util.ext.localFirstThenRepoIfNull
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.lib.db.composites.ContentEntryAndDetail
import com.ustadmobile.lib.db.composites.OfflineItemAndState
import com.ustadmobile.lib.db.composites.TransferJobAndTotals
import io.github.aakira.napier.Napier
import org.kodein.di.instance
import org.kodein.di.instanceOrNull

data class ContentEntryDetailOverviewUiState(

    val scoreProgress: ContentEntryStatementScoreProgress? = null,

    val contentEntry: ContentEntryAndDetail? = null,

    val contentEntryButtons: ContentEntryButtonModel? = null,

    val locallyAvailable: Boolean = false,

    val markCompleteVisible: Boolean = false,

    val translationVisibile: Boolean = false,

    val availableTranslations: List<ContentEntryRelatedEntryJoinWithLanguage> = emptyList(),

    val activeContentJobItems: List<ContentJobItemProgress> = emptyList(),

    val activeUploadJobs: List<TransferJobAndTotals> = emptyList(),

    val offlineItemAndState: OfflineItemAndState? = null,

    val openButtonEnabled: Boolean = true,
) {
    val scoreProgressVisible: Boolean
        get() = scoreProgress?.progress != null && scoreProgress.progress > 0

    val authorVisible: Boolean
        get() = !contentEntry?.entry?.author.isNullOrBlank()

    val publisherVisible: Boolean
        get() = !contentEntry?.entry?.publisher.isNullOrBlank()

    val licenseNameVisible: Boolean
        get() = !contentEntry?.entry?.licenseName.isNullOrBlank()

    val fileSizeVisible: Boolean
        get() = false

    val scoreResultVisible: Boolean
        get() = scoreProgress != null

    val openButtonVisible: Boolean
        get() = true

}

class ContentEntryDetailOverviewViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : DetailViewModel<ContentEntry> (di, savedStateHandle, DEST_NAME){

    private val _uiState = MutableStateFlow(
        ContentEntryDetailOverviewUiState())

    /*
     * Make Content Entry Available Offline Use Case will create the offline item in the database.
     */
    private val makeContentEntryAvailableOfflineUseCase: MakeContentEntryAvailableOfflineUseCase by
            di.onActiveEndpoint().instance()

    private val cancelDownloadUseCase: CancelDownloadUseCase by di.onActiveEndpoint().instance()

    val nodeIdAndAuth: NodeIdAndAuth by di.onActiveEndpoint().instance()

    val uiState: Flow<ContentEntryDetailOverviewUiState> = _uiState.asStateFlow()

    private val defaultLaunchContentEntryUseCase: LaunchContentEntryVersionUseCase by di
        .onActiveEndpoint().instance()

    private val launchXapiUseCase: LaunchXapiUseCase? by di.onActiveEndpoint().instanceOrNull()

    private val launchEpubUseCase: LaunchEpubUseCase? by di.onActiveEndpoint().instanceOrNull()

    private val target = savedStateHandle[ARG_TARGET]


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
                                title = it?.entry?.title ?: ""
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
                                        visible =  hasPermission,
                                        text = systemImpl.getString(MR.strings.edit),
                                        icon = FabUiState.FabIcon.EDIT,
                                        onClick = ::onClickEdit
                                    )
                                )
                            }else {
                                prev
                            }
                        }
                    }
                }

                launch {
                    activeDb.transferJobDao.findByContentEntryUidWithTotalsAsFlow(
                        contentEntryUid = entityUidArg,
                        jobType = TransferJob.TYPE_BLOB_UPLOAD,
                    ).collect {
                        _uiState.update { prev ->
                            prev.copy(
                                activeUploadJobs = it
                            )
                        }
                    }
                }

                launch {
                    activeDb.offlineItemDao.findByContentEntryUid(
                        contentEntryUid = entityUidArg,
                        nodeId = nodeIdAndAuth.nodeId
                    ).collect {
                        _uiState.update { prev ->
                            prev.copy(
                                offlineItemAndState = it
                            )
                        }
                    }
                }
            }
        }
    }

    fun onClickEdit() {

    }


    fun onClickOffline() {
        viewModelScope.launch {
            val offlineItemAndStateVal = _uiState.value.offlineItemAndState
            val offlineItemVal = offlineItemAndStateVal?.offlineItem
            val activeDownload = offlineItemAndStateVal?.activeDownload

            when {
                //Not available for offline use yet, mark as selected for offline and start download
                offlineItemVal == null || !offlineItemVal.oiActive -> {
                    makeContentEntryAvailableOfflineUseCase(contentEntryUid = entityUidArg)
                }

                //Currently in progress, if clicked, cancel
                activeDownload != null -> {
                    cancelDownloadUseCase(
                        transferJobId = activeDownload.transferJob?.tjUid ?: 0,
                        offlineItemUid = offlineItemVal.oiUid
                    )
                }

                //There is an offline item, transfer was completed, we can set the offline item inactive
                //The trigger created by AddOfflineItemInactiveTriggersCallback will set the
                //remove CacheLockJoin(s) status to pending deletion so cache content becomes
                // eligible for eviction as required.
                offlineItemAndStateVal.readyForOffline -> {
                    activeRepo.offlineItemDao.updateActiveByOfflineItemUid(offlineItemVal.oiUid, false)
                }
            }
        }
    }

    fun onClickOpen() {
        viewModelScope.launch {
            try {
                loadingState = LoadingUiState.INDETERMINATE
                _uiState.update { it.copy(openButtonEnabled = false) }
                val latestContentEntryVersion = activeRepo.localFirstThenRepoIfNull {
                    it.contentEntryVersionDao.findLatestVersionUidByContentEntryUidEntity(entityUidArg)
                }

                val openTarget = target?.let {
                    OpenExternalLinkUseCase.Companion.LinkTarget.of(it)
                } ?: OpenExternalLinkUseCase.Companion.LinkTarget.DEFAULT

                if(latestContentEntryVersion != null) {
                    val contentSpecificLauncher = when(latestContentEntryVersion.cevContentType) {
                        ContentEntryVersion.TYPE_XAPI -> launchXapiUseCase
                        ContentEntryVersion.TYPE_EPUB -> launchEpubUseCase
                        else -> null
                    }

                    val launcher = (contentSpecificLauncher ?: defaultLaunchContentEntryUseCase)
                    launcher(latestContentEntryVersion, navController, openTarget)
                }else {
                    snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.content_not_ready_try_later)))
                }
            }catch(e: Throwable) {
                snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.error) + ":${e.message}"))
                Napier.w("ContentEntryDetailOverview: Exception opening content", e)
            }finally {
                loadingState = LoadingUiState.NOT_LOADING
                _uiState.update { it.copy(openButtonEnabled = true) }
            }
        }
    }

    companion object {

        const val DEST_NAME = "ContentEntryDetailOverviewView"

        const val ARG_TARGET = "target"

    }

}