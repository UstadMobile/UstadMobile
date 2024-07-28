package com.ustadmobile.core.viewmodel.contententry.detailoverviewtab

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
import com.ustadmobile.core.domain.contententry.importcontent.CancelImportContentEntryUseCase
import com.ustadmobile.core.domain.contententry.importcontent.CancelRemoteContentEntryImportUseCase
import com.ustadmobile.core.domain.contententry.importcontent.DismissRemoteContentEntryImportErrorUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.LaunchContentEntryVersionUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.epub.LaunchEpubUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.xapi.LaunchXapiUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.util.ext.bodyAsDecodedText
import com.ustadmobile.core.util.ext.localFirstThenRepoIfNull
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.clazz.launchSetTitleFromClazzUid
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.lib.db.composites.ContentEntryAndDetail
import com.ustadmobile.lib.db.composites.ContentEntryImportJobProgress
import com.ustadmobile.lib.db.composites.OfflineItemAndState
import com.ustadmobile.lib.db.composites.TransferJobAndTotals
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.instance
import org.kodein.di.instanceOrNull

data class ContentEntryDetailOverviewUiState(

    val scoreProgress: ContentEntryStatementScoreProgress? = null,

    val contentEntry: ContentEntryAndDetail? = null,

    val latestContentEntryVersion: ContentEntryVersion? = null,

    val contentEntryButtons: ContentEntryButtonModel? = null,

    val locallyAvailable: Boolean = false,

    val markCompleteVisible: Boolean = false,

    val translationVisibile: Boolean = false,

    val availableTranslations: List<ContentEntryRelatedEntryJoinWithLanguage> = emptyList(),

    val activeImportJobs: List<ContentEntryImportJobProgress> = emptyList(),

    val remoteImportJobs: List<ContentEntryImportJobProgress> = emptyList(),

    val activeUploadJobs: List<TransferJobAndTotals> = emptyList(),

    val offlineItemAndState: OfflineItemAndState? = null,

    val openButtonEnabled: Boolean = true,

    val activeUserPersonUid: Long = 0,
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

    val compressedSizeVisible: Boolean
        get() = latestContentEntryVersion?.let {
            it.cevStorageSize > 0 && it.cevStorageSize > 0 && it.cevStorageSize < it.cevOriginalSize
        } ?: false

    val sizeVisible: Boolean
        get() = latestContentEntryVersion?.let {
            it.cevStorageSize > 0
        } ?: false

    fun canCancelRemoteImportJob(importJobProgress: ContentEntryImportJobProgress): Boolean {
        return importJobProgress.cjiOwnerPersonUid == activeUserPersonUid
    }

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

    private val cancelImportContentEntryUseCase: CancelImportContentEntryUseCase? by
        di.onActiveEndpoint().instanceOrNull()

    private val cancelRemoteContentEntryImportUseCase: CancelRemoteContentEntryImportUseCase by
        di.onActiveEndpoint().instance()

    private val dismissRemoteContentEntryImportErrorUseCase: DismissRemoteContentEntryImportErrorUseCase by
        di.onActiveEndpoint().instance()

    private val httpClient: HttpClient by di.instance()

    private val clazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: 0

    private val parentEntryUid = savedStateHandle[ARG_PARENT_UID]?.toLong() ?: 0

    init {
        _uiState.update { it.copy(activeUserPersonUid = activeUserPersonUid) }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepo.contentEntryDao().findByContentEntryUidWithDetailsAsFlow(
                        contentEntryUid = entityUidArg,
                        clazzUid = clazzUid,
                        courseBlockUid = savedStateHandle[ARG_COURSE_BLOCK_UID]?.toLong() ?: 0,
                        accountPersonUid = activeUserPersonUid,
                    ).collect {
                        _uiState.update { prev ->
                            prev.copy(
                                contentEntry = it
                            )
                        }
                    }
                }

                if(clazzUid == 0L && parentEntryUid != 0L) {
                    launch {
                        val parentTitle = if(parentEntryUid == ContentEntryListViewModel.LIBRARY_ROOT_CONTENT_ENTRY_UID) {
                            systemImpl.getString(MR.strings.library)
                        }else {
                            activeRepo.localFirstThenRepoIfNull { db ->
                                db.contentEntryDao().findTitleByUidAsync(parentEntryUid)
                            }
                        }

                        _appUiState.update { it.copy(title = parentTitle) }
                    }
                }

                launchSetTitleFromClazzUid(clazzUid) { title ->
                    _appUiState.update { it.copy(title = title) }
                }

                launch {
                    activeRepo.contentEntryVersionDao().findLatestByContentEntryUidAsFlow(
                        contentEntryUid = entityUidArg
                    ).collect{
                        _uiState.update { prev ->
                            prev.copy(
                                latestContentEntryVersion = it
                            )
                        }
                    }
                }

                launch {
                    activeDb.transferJobDao().findByContentEntryUidWithTotalsAsFlow(
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
                    activeDb.contentEntryImportJobDao().findInProgressJobsByContentEntryUid(
                        contentEntryUid = entityUidArg
                    ).collect {
                        _uiState.update { prev ->
                            prev.copy(
                                activeImportJobs = it
                            )
                        }
                    }
                }

                launch {
                    activeDb.offlineItemDao().findByContentEntryUid(
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

                launch {
                    try {
                        do {
                            val remoteImportJobsJson = httpClient.get(
                                "${accountManager.activeEndpoint.url}api/contententryimportjob/importjobs"
                            ) {
                                parameter("contententryuid", entityUidArg.toString())
                                header("cache-control", "no-store")
                            }.bodyAsDecodedText()
                            val remoteImportJobs = json.decodeFromString(
                                ListSerializer(ContentEntryImportJobProgress.serializer()),
                                remoteImportJobsJson
                            )

                            val state = _uiState.updateAndGet { prev ->
                                prev.copy(
                                    remoteImportJobs = remoteImportJobs
                                )
                            }
                        } while(state.remoteImportJobs.isNotEmpty())
                    }catch(e: Throwable) {
                        Napier.d { "Could not get list of jobs" }
                    }
                }
            }
        }
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
                    activeRepo.offlineItemDao().updateActiveByOfflineItemUid(offlineItemVal.oiUid, false)
                }
            }
        }
    }

    fun onClickOpen() {
        Napier.d("ContentEntryDetailOverviewViewModel: onClickOpen")
        viewModelScope.launch {
            try {
                loadingState = LoadingUiState.INDETERMINATE
                Napier.d("ContentEntryDetailOverviewViewModel: onClickOpen launched")
                _uiState.update { it.copy(openButtonEnabled = false) }
                val latestContentEntryVersion = activeRepo.localFirstThenRepoIfNull {
                    it.contentEntryVersionDao().findLatestVersionUidByContentEntryUidEntity(entityUidArg)
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
                    Napier.d("ContentEntryDetailOverviewViewModel: onClickOpen : Launching using $launcher")
                    launcher(
                        contentEntryVersion = latestContentEntryVersion,
                        navController = navController,
                        target = openTarget,
                        xapiSession = createXapiSession(contentEntryUid = entityUidArg),
                    )
                }else {
                    Napier.d("ContentEntryDetailOverviewViewModel: onClickOpen: latestContentEntryVersion = null")
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

    fun onCancelImport(jobUid: Long) {
        viewModelScope.launch {
            cancelImportContentEntryUseCase?.invoke(jobUid)
            snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.canceled)))
        }
    }

    fun onCancelRemoteImport(jobUid: Long) {
        viewModelScope.launch {
            try {
                cancelRemoteContentEntryImportUseCase(jobUid, activeUserPersonUid)
                snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.canceled)))
            }catch(e: Throwable) {
                snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.error)))
            }
        }
    }

    fun onDismissImportError(jobUid: Long) {
        viewModelScope.launch {
            activeDb.contentEntryImportJobDao().updateErrorDismissed(jobUid, true)
        }
    }

    fun onDismissRemoteImportError(jobUid: Long) {
        viewModelScope.launch {
            try{
                dismissRemoteContentEntryImportErrorUseCase(jobUid, activeUserPersonUid)
            }catch(e: Throwable) {
                //Error dismissing error? Unlucky day
                Napier.w { "ContentEntryDetailoverview: could not dismiss remote error message"}
                snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.error)))
            }
        }
    }

    fun onDismissUploadError(jobUid: Int) {
        viewModelScope.launch {
            activeDb.transferJobErrorDao().dismissErrorByJobId(jobUid, true)
        }
    }

    companion object {

        const val DEST_NAME = "ContentEntryDetailOverviewView"

        const val ARG_TARGET = "target"

    }

}