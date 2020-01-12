package com.ustadmobile.sharedse.controller

import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.isStatusCompleted
import com.ustadmobile.core.util.ext.isStatusCompletedSuccessfully
import com.ustadmobile.core.util.ext.isStatusPaused
import com.ustadmobile.core.util.ext.isStatusPausedOrQueuedOrDownloading
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.liveDataObserverDispatcher
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.lib.db.entities.DownloadJobSizeInfo
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import com.ustadmobile.sharedse.network.requestDelete
import com.ustadmobile.sharedse.network.requestDownloadPreparation
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.jvm.Volatile

class DownloadDialogPresenter(context: Any,
                              arguments: Map<String, String>, view: DownloadDialogView,
                              private var appDatabase: UmAppDatabase,
                              private val appDatabaseRepo: UmAppDatabase,
                              private val containerDownloadManager: ContainerDownloadManager,
                              private val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                              private val downloadJobPreparationRequester: (Int, Any) -> Unit = ::requestDownloadPreparation)
    : UstadBaseController<DownloadDialogView>(context, arguments, view), DoorObserver<DownloadJob?> {

    private var deleteFileOptions = false

    private var contentEntryUid = 0L


    /**
     * Testing purpose
     */
    @Volatile
    var currentJobId: Int = -1
        private set

    private var statusMessage: String? = null

    //private var destinationDir: String? = null

    private val jobSizeLoading = atomic(false)

    private val jobSizeTotals = atomic(null as DownloadJobSizeInfo?)

    private val wifiOnlyChecked = atomic(false)

    private lateinit var downloadJobItemLiveData : DoorLiveData<DownloadJobItem?>

    private var currentDownloadJobItem: DownloadJobItem? = null

    private var downloadJobLiveData: DoorLiveData<DownloadJob?>? = null

    private var selectedStorageDir: UMStorageDir? = null

    private val downloadJobItemObserver = object: DoorObserver<DownloadJobItem?> {
        override fun onChanged(t: DownloadJobItem?) {
            currentDownloadJobItem = t
            val newDownloadJobIdVal = t?.djiDjUid ?: 0
            if(newDownloadJobIdVal != currentJobId) {
                currentJobId = newDownloadJobIdVal
                GlobalScope.launch(liveDataObserverDispatcher()) {
                    val downloadJobLiveDataVal = containerDownloadManager.getDownloadJob(newDownloadJobIdVal)
                    downloadJobLiveData = downloadJobLiveDataVal
                    downloadJobLiveDataVal.observe(context as DoorLifecycleOwner,
                            this@DownloadDialogPresenter)
                }
            }
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        contentEntryUid = arguments[UstadView.ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0L
        UMLog.l(UMLog.INFO, 420, "Starting download presenter for " +
                "content entry uid: " + contentEntryUid)
        view.setWifiOnlyOptionVisible(false)
        GlobalScope.launch(liveDataObserverDispatcher()) {
            downloadJobItemLiveData = containerDownloadManager.getDownloadJobItemByContentEntryUid(
                    contentEntryUid)
            val isWifiOnly = !appDatabase.downloadJobDao.getMeteredNetworkAllowed(currentJobId)
            wifiOnlyChecked.value = isWifiOnly
            view.setDownloadOverWifiOnly(isWifiOnly)
            downloadJobItemLiveData.observe(this@DownloadDialogPresenter.context as DoorLifecycleOwner,
                    downloadJobItemObserver)

            val storageDirs = impl.getStorageDirsAsync(context)
            view.runOnUiThread(Runnable {
                selectedStorageDir = storageDirs.firstOrNull()
                view.showStorageOptions(storageDirs)
                updateWarningMessage(downloadJobItemLiveData.getValue())
            })
        }
    }

    override fun onChanged(t: DownloadJob?) {
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
                && !jobSizeLoading.compareAndSet(true, true)) {
            view.setBottomPositiveButtonEnabled(false)
            GlobalScope.launch {
                try {
                    val sizeTotals = if(t != null) {
                        appDatabase.downloadJobDao.getDownloadSizeInfo(t.djUid)
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
                updateWarningMessage(downloadJobItemLiveData.getValue())
                view.setCalculatingViewVisible(false)
                view.setStatusText(currentStatuMessage,
                    downloadTotals.numEntries, UMFileUtil.formatFileSize(downloadTotals.totalSize))
            })
        }
    }

    private fun updateWarningMessage(currentDownloadJobItem: DownloadJobItem?) {
        val jobSizeTotalsVal = jobSizeTotals.value
        val selectedStorageDirVal = selectedStorageDir
        val currentStatus = currentDownloadJobItem?.djiStatus ?: 0
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

    private suspend fun createDownloadJobAndRequestPreparation() : Boolean{
        val newDownloadJob = DownloadJob(contentEntryUid, getSystemTimeInMillis())
        newDownloadJob.djDestinationDir = selectedStorageDir?.dirURI
        newDownloadJob.djStatus = JobStatus.NEEDS_PREPARED
        val isWifiOnlyChecked = wifiOnlyChecked.value
        newDownloadJob.meteredNetworkAllowed = !isWifiOnlyChecked
        containerDownloadManager.createDownloadJob(newDownloadJob)
        currentJobId = newDownloadJob.djUid
        downloadJobPreparationRequester(currentJobId, context)
        return currentJobId != 0
    }


    /**
     * The positive button can be either the download button or the download button
     */
    fun handleClickPositive() {
        val currentDownloadJobItemVal = currentDownloadJobItem
        when {
            currentDownloadJobItem.isStatusCompletedSuccessfully() && currentDownloadJobItemVal != null ->
                requestDelete(currentDownloadJobItemVal.djiDjUid, containerDownloadManager, context)

            currentDownloadJobItem.isStatusPaused() && currentDownloadJobItemVal != null -> GlobalScope.launch {
                containerDownloadManager.enqueue(currentDownloadJobItemVal.djiDjUid)
            }

            else -> GlobalScope.launch {
                createDownloadJobAndRequestPreparation()
            }
        }
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
        val currentDownloadJobItemVal = currentDownloadJobItem
        if(currentDownloadJobItemVal != null) {
            when (idClicked) {
                STACKED_BUTTON_PAUSE -> GlobalScope.launch {
                    containerDownloadManager.pause(currentDownloadJobItemVal.djiDjUid)
                }

                //The continue button will do NOTHING - the download is already running

                STACKED_BUTTON_CANCEL -> GlobalScope.launch {
                    containerDownloadManager.cancel(currentDownloadJobItemVal.djiDjUid)
                }
            }

            dismissDialog()
        }else {
            //something is wrong - we should not have been able to get here...
        }

    }

    private fun dismissDialog() {
        view.runOnUiThread(Runnable { view.dismissDialog() })
    }

    fun handleClickWiFiOnlyOption(wifiOnly: Boolean) {
        wifiOnlyChecked.value = wifiOnly
        if(currentJobId != 0) {
            GlobalScope.launch {
                containerDownloadManager.setMeteredDataAllowed(currentJobId, !wifiOnly)
            }
        }
    }

    fun handleStorageOptionSelection(selectedDir: UMStorageDir) {
        selectedStorageDir = selectedDir
        updateWarningMessage(downloadJobItemLiveData.getValue())
        GlobalScope.launch {
            val downloadJob = containerDownloadManager.getDownloadJob(currentJobId).getValue()
            if(downloadJob != null){
                containerDownloadManager.handleDownloadJobUpdated(downloadJob.also {
                    it.djDestinationDir = selectedDir.dirURI
                })
            }

            appDatabase.downloadJobDao.updateDestinationDirectoryAsync(currentJobId,
                    selectedDir.dirURI)
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
