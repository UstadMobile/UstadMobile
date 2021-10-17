package com.ustadmobile.sharedse.controller

import io.github.aakira.napier.Napier
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import com.ustadmobile.core.util.ext.isStatusCompletedSuccessfully
import com.ustadmobile.core.util.ext.isStatusPausedOrQueuedOrDownloading
import com.ustadmobile.door.*
import com.ustadmobile.lib.db.entities.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlin.jvm.Volatile
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on


class DownloadDialogPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: DownloadDialogView,
    di: DI,
    private val lifecycleOwner: DoorLifecycleOwner
) : UstadBaseController<DownloadDialogView>(context, arguments, view, di), DoorObserver<ContentJobItem?> {

    private var deleteFileOptions = false

    private var contentEntryUid = 0L

    /**
     * Testing purpose
     */
    @Volatile
    var currentJobId: Long = -1
        private set

    private var statusMessage: String? = null

    private val jobSizeLoading = atomic(false)

    private val jobSizeTotals = atomic(null as DownloadJobSizeInfo?)

    private val wifiOnlyChecked = atomic(0)

    private lateinit var contentJobItemLiveData : DoorLiveData<ContentJobItem?>

    //Used to avoid issues with handleStorageOptionSelection being called before the
    // downloadJobItemLiveData is ready.
    private val contentJobCompletable = CompletableDeferred<Boolean>()

    private var currentContentJobItem: ContentJobItem? = null

    private var selectedStorageDir: ContainerStorageDir? = null

    private val accountManager: UstadAccountManager by instance()

    private val impl: UstadMobileSystemImpl by instance()

    private val contentJobManager: ContentJobManager by di.instance()

    private val appDatabase: UmAppDatabase by on(accountManager.activeAccount).instance(tag = TAG_DB)

    private val appDatabaseRepo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = TAG_REPO)

    /**
     * If this is private, Kotlinx serialization will refuse to compile compileKotlinMetadata
     */
/*    val downloadJobItemObserver = DoorObserver<ContentJobItem?> { t ->
        currentContentJobItem = t
        val newDownloadJobIdVal = t?.cjiJobUid ?: 0
        if(newDownloadJobIdVal != currentJobId) {
            currentJobId = newDownloadJobIdVal
           *//* GlobalScope.launch(doorMainDispatcher()) {
                val downloadJobLiveDataVal = appDatabase.contentJobDao.findLiveDataByUid(newDownloadJobIdVal)
                contentJobLiveData = downloadJobLiveDataVal
                downloadJobLiveDataVal.observe(lifecycleOwner,
                        this@DownloadDialogPresenter)
            }*//*
        }
    }*/

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        contentEntryUid = arguments[UstadView.ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0L
        Napier.i("Starting download presenter for $contentEntryUid")
        view.setWifiOnlyOptionVisible(false)
        GlobalScope.launch {
            contentJobItemLiveData = appDatabase.contentJobItemDao.findLiveDataByContentEntryUid(
                    contentEntryUid)
            contentJobCompletable.complete(true)
            // TODO check downloadJobItem if status = wifiOnly
            val job =  contentJobItemLiveData.getValue()
            val wifiOnly: Boolean = job == null || job.cjiConnectivityAcceptable == ContentJobItem.ACCEPT_METERED
            view.setDownloadOverWifiOnly(wifiOnly)
            val wifiCheckValue = job?.cjiConnectivityAcceptable ?: ContentJobItem.ACCEPT_METERED
            wifiOnlyChecked.value = wifiCheckValue

            view.runOnUiThread(Runnable {
                contentJobItemLiveData.observe(lifecycleOwner, this@DownloadDialogPresenter)
            })

            updateWarningMessage(job)
        }
    }

    override fun onChanged(t: ContentJobItem?) {
        currentContentJobItem = t
        val newDownloadJobIdVal = t?.cjiJobUid ?: 0
        if(newDownloadJobIdVal != currentJobId) {
            currentJobId = newDownloadJobIdVal
        }

        when {
            t.isStatusCompletedSuccessfully() -> {
                deleteFileOptions = true
                view.setCalculatingViewVisible(false)
                view.setStackOptionsVisible(false)
                view.setBottomButtonsVisible(true)
                statusMessage = impl.getString(MessageID.download_state_downloaded,
                        context)
                view.setBottomButtonPositiveText(impl.getString(
                        MessageID.delete, context))
                view.setBottomButtonNegativeText(impl.getString(
                        MessageID.cancel, context))
                view.setWifiOnlyOptionVisible(false)
            }

            t.isStatusPausedOrQueuedOrDownloading() -> {
                view.setCalculatingViewVisible(false)
                deleteFileOptions = false
                view.setStackOptionsVisible(true)
                view.setBottomButtonsVisible(false)
                val optionTexts = STACKED_TEXT_MESSAGE_IDS.map {impl.getString(it, context)}.toTypedArray()
                statusMessage = impl.getString(MessageID.download_state_downloading,
                        context)
                view.setStackedOptions(STACKED_OPTIONS, optionTexts)
                view.setWifiOnlyOptionVisible(true)
            }

            else -> {
                deleteFileOptions = false
                statusMessage = impl.getString(MessageID.download_state_download,
                        context)
                view.setStackOptionsVisible(false)
                view.setBottomButtonsVisible(true)
                view.setBottomButtonPositiveText(impl.getString(
                        MessageID.download, context))
                view.setBottomButtonNegativeText(impl.getString(
                        MessageID.cancel, context))
                view.setWifiOnlyOptionVisible(true)
            }

        }

        val currentJobSizeTotals = jobSizeTotals.value
        if(!t.isStatusPausedOrQueuedOrDownloading() && currentJobSizeTotals == null
                && !jobSizeLoading.compareAndSet(expect = true, update = true)) {
            view.setBottomPositiveButtonEnabled(false)
            GlobalScope.launch {
                try {
                    val sizeTotals = if(t != null) {
                        appDatabase.contentJobItemDao.getDownloadSizeInfo(t.cjiJobUid)
                    }else {
                        appDatabaseRepo.contentEntryDao.getRecursiveDownloadTotals(contentEntryUid)
                    }
                    jobSizeTotals.value = sizeTotals
                    updateStatusMessage(sizeTotals)
                }catch(e: Exception) {
                    view.runOnUiThread(Runnable {
                        view.setCalculatingViewVisible(false)
                        view.setWarningTextVisible(true)
                        view.setWifiOnlyOptionVisible(false)
                        view.setWarningText(impl.getString(MessageID.repo_loading_status_failed_noconnection,
                                context))
                    })

                }finally {
                    jobSizeLoading.value = false
                }
            }
        }else if(currentJobSizeTotals != null) {
            updateStatusMessage(currentJobSizeTotals)
        }
    }

    private fun updateStatusMessage(downloadTotals: DownloadJobSizeInfo?) {
        val currentStatuMessage = statusMessage
        if(downloadTotals != null && currentStatuMessage != null){
            view.runOnUiThread(Runnable {
                updateWarningMessage(contentJobItemLiveData.getValue())
                view.setCalculatingViewVisible(false)
                view.setStatusText(currentStatuMessage,
                    downloadTotals.numEntries, UMFileUtil.formatFileSize(downloadTotals.totalSize))
            })
        }
    }

    private fun updateWarningMessage(currentDownloadJobItem: ContentJobItem?) {
        val jobSizeTotalsVal = jobSizeTotals.value
        val selectedStorageDirVal = selectedStorageDir
        val currentStatus = currentDownloadJobItem?.cjiStatus ?: 0
        if(currentStatus <= JobStatus.PAUSED && jobSizeTotalsVal != null
                && selectedStorageDirVal != null) {
            if(jobSizeTotalsVal.totalSize > selectedStorageDirVal.usableSpace) {
                view.setWarningTextVisible(true)
                view.setWarningText(impl.getString(MessageID.insufficient_space, context))
                view.setBottomPositiveButtonEnabled(false)
            }else {
                view.setWarningTextVisible(false)
                view.setBottomPositiveButtonEnabled(true)
            }
        }else {
            view.setBottomPositiveButtonEnabled(true)
        }
    }

    private suspend fun createDownloadJob() : Boolean{

        val entry = appDatabaseRepo.contentEntryDao.findByUid(contentEntryUid)
        val container = appDatabaseRepo.containerDao
                .getMostRecentDownloadedContainerForContentEntryAsync(contentEntryUid)
        val isWifiOnlyChecked = wifiOnlyChecked.value
        val job = ContentJob().apply {
            toUri = selectedStorageDir?.dirUri
            cjNotificationTitle = impl.getString(MessageID.downloading_content, context)
                    .replace("%1\$s",entry?.title ?: "")
            cjUid = appDatabase.contentJobDao.insertAsync(this)
        }
        currentJobId = job.cjUid
        ContentJobItem().apply {
            cjiJobUid = job.cjUid
            sourceUri = "" // TODO entry ?
            cjiItemTotal = container?.fileSize ?: 0
            cjiPluginId = 10 // TODO containerTorrentDownload plugin ref in core commonMain
            cjiContentEntryUid = contentEntryUid
            cjiContainerUid = container?.containerUid ?: 0
            cjiIsLeaf = entry?.leaf ?: false
            cjiConnectivityAcceptable = isWifiOnlyChecked
            cjiStatus = JobStatus.QUEUED
            cjiUid = appDatabase.contentJobItemDao.insertJobItem(this)
        }

        contentJobManager.enqueueContentJob(accountManager.activeEndpoint, job.cjUid)

        return currentJobId != 0L
    }


    /**
     * The positive button can be either the download button or the download button
     */
    fun handleClickPositive() {
        val currentDownloadJobItemVal = currentContentJobItem
        when {
            currentContentJobItem?.cjiStatus == JobStatus.COMPLETE && currentDownloadJobItemVal != null -> GlobalScope.launch {
                createDeleteJob()
            }
            currentContentJobItem?.cjiStatus == JobStatus.PAUSED && currentDownloadJobItemVal != null -> GlobalScope.launch {
                // TODO queue it
                //containerDownloadManager.enqueue(currentDownloadJobItemVal.cjiJobUid)
            }

            else -> GlobalScope.launch {
                createDownloadJob()
            }
        }
    }

    private suspend fun createDeleteJob() {
        val entry = appDatabaseRepo.contentEntryDao.findByUid(contentEntryUid)
        val job = ContentJob().apply {
            cjNotificationTitle = impl.getString(MessageID.deleting_content, context)
                    .replace("%1\$s",entry?.title ?: "")
            cjUid = appDatabase.contentJobDao.insertAsync(this)
        }
        ContentJobItem().apply {
            cjiJobUid = job.cjUid
            sourceUri = "" // TODO entry
            cjiPluginId = 14 // points to deleteContainerPlugin
            cjiContentEntryUid = entry?.contentEntryUid ?: 0
            cjiIsLeaf = true
            cjiParentContentEntryUid = 0
            cjiConnectivityAcceptable = ContentJobItem.ACCEPT_ANY
            cjiStatus = JobStatus.QUEUED
            cjiUid = appDatabase.contentJobItemDao.insertJobItem(this)
        }

        contentJobManager.enqueueContentJob(accountManager.activeEndpoint, job.cjUid)
    }

    /**
     * Handle negative click. If the underlying system is already dismissing the dialog
     * set dismissAfter to false to avoid a call to dismissDialog
     * @param dismissAfter flag to indicate if the dialog will be dismissed after the selection
     */
    fun handleClickNegative(dismissAfter: Boolean = true) {
        if(dismissAfter)
            dismissDialog()
    }



    fun handleClickStackedButton(idClicked: Int) {
        val currentDownloadJobItemVal = currentContentJobItem
        if(currentDownloadJobItemVal != null) {
            when (idClicked) {
                STACKED_BUTTON_PAUSE -> GlobalScope.launch {
                    // TODO pause download
                    //containerDownloadManager.pause(currentDownloadJobItemVal.djiDjUid)
                }

                //If the download is already running, this will have no effect
                STACKED_BUTTON_CONTINUE -> GlobalScope.launch {
                    // TODO back to running download
                    //containerDownloadManager.enqueue(currentDownloadJobItemVal.djiDjUid)
                }

                STACKED_BUTTON_CANCEL -> GlobalScope.launch {
                    // TODO cancel download
                    //containerDownloadManager.cancel(currentDownloadJobItemVal.djiDjUid)
                }
            }

            dismissDialog()
        }
    }

    private fun dismissDialog() {
        view.runOnUiThread(Runnable { view.dismissDialog() })
    }

    fun handleClickWiFiOnlyOption(wifiOnly: Boolean) {
        val wifiOnlyCheckedVal = if(wifiOnly) ContentJobItem.ACCEPT_METERED else ContentJobItem.ACCEPT_UNMETERED
        wifiOnlyChecked.value = wifiOnlyCheckedVal
        if(currentJobId != 0L) {
            GlobalScope.launch {
                // TODO handle wifi status change
                appDatabase.contentJobItemDao.updateConnectivityStatus(currentJobId, wifiOnlyCheckedVal)
            }
        }
    }

    fun handleStorageOptionSelection(selectedDir: ContainerStorageDir) {
        selectedStorageDir = selectedDir
        GlobalScope.launch(doorMainDispatcher()) {
            contentJobCompletable.await()
            updateWarningMessage(contentJobItemLiveData.getValue())
            val contentJob = appDatabase.contentJobDao.findByUidAsync(currentJobId)
            if(contentJob != null){
                appDatabase.contentJobDao.updateDestinationDir(currentJobId, selectedDir.dirUri)
                // TODO handle job updated save location
                /*containerDownloadManager.handleDownloadJobUpdated(downloadJob.also {
                    it.djDestinationDir = selectedDir.dirURI
                })*/
            }
            /*
            appDatabase.downloadJobDao.updateDestinationDirectoryAsync(currentJobId,
                    selectedDir.dirURI)*/
        }
    }

    companion object {

        const val STACKED_BUTTON_PAUSE = 0

        const val STACKED_BUTTON_CANCEL = 1

        const val STACKED_BUTTON_CONTINUE = 2

        //Previously internal: This does not compile since Kotlin 1.3.61
        val STACKED_OPTIONS = intArrayOf(STACKED_BUTTON_PAUSE, STACKED_BUTTON_CANCEL,
                STACKED_BUTTON_CONTINUE)

        //Previously internal: This does not compile since Kotlin 1.3.61
        val STACKED_TEXT_MESSAGE_IDS = listOf(MessageID.pause_download,
                MessageID.download_cancel_label, MessageID.download_continue_stacked_label)
    }
}
