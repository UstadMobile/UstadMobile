package com.ustadmobile.sharedse.controller

import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.OnDownloadJobItemChangeListener
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.observe
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import com.ustadmobile.lib.db.entities.DownloadJobSizeInfo
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import com.ustadmobile.sharedse.network.DownloadJobItemManager
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon
import com.ustadmobile.sharedse.network.requestDelete
import com.ustadmobile.sharedse.network.requestDownloadPreparation
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.jvm.Volatile

class DownloadDialogPresenter(context: Any, private val networkManagerBle: NetworkManagerBleCommon,
                              arguments: Map<String, String>, view: DownloadDialogView,
                              private var appDatabase: UmAppDatabase, private val appDatabaseRepo: UmAppDatabase,
                              private val downloadJobPreparationRequester: (Int, Any) -> Unit = ::requestDownloadPreparation)
    : UstadBaseController<DownloadDialogView>(context, arguments, view), OnDownloadJobItemChangeListener {

    private var deleteFileOptions = false

    private var contentEntryUid = 0L

    private lateinit var downloadDownloadJobLive: DoorLiveData<DownloadJob?>

    /**
     * Testing purpose
     */
    @Volatile
    var currentJobId: Int = 0
        private set

    private lateinit var impl: UstadMobileSystemImpl

    private var statusMessage: String? = null

    private var destinationDir: String? = null

    private var jobItemManager: DownloadJobItemManager? = null

    private val downloadJobStatus = atomic(0)

    private val jobSizeLoading = atomic(false)

    private val jobSizeTotals = atomic(null as DownloadJobSizeInfo?)

    private val wifiOnlyChecked = atomic(false)

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        appDatabase = UmAppDatabase.getInstance(context)

        impl = UstadMobileSystemImpl.instance
        contentEntryUid = arguments[ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0L
        UMLog.l(UMLog.INFO, 420, "Starting download presenter for " +
                "content entry uid: " + contentEntryUid)
        view.setWifiOnlyOptionVisible(false)

        impl.getStorageDirs(context, object : UmResultCallback<List<UMStorageDir>>{
            override fun onDone(result: List<UMStorageDir>?) {
                destinationDir = result?.get(0)?.dirURI
                view.runOnUiThread(Runnable{ view.showStorageOptions(result!!) })
                GlobalScope.launch {
                    setup()
                }
            }
        })

        networkManagerBle.addDownloadChangeListener(this)
    }

    private suspend fun setup() {
        currentJobId = appDatabase.downloadJobDao
                .getLatestDownloadJobUidForContentEntryUid(contentEntryUid)

        if (currentJobId != 0) {
            jobItemManager = networkManagerBle.openDownloadJobItemManager(currentJobId)!!
        }

        startObservingJobAndMeteredState()
    }

    private fun startObservingJobAndMeteredState() {
        if(currentJobId != 0) {
            GlobalScope.launch {
                val isWifiOnly = !appDatabase.downloadJobDao
                        .getMeteredNetworkAllowed(currentJobId)
                wifiOnlyChecked.value = isWifiOnly
                with(view) {
                    runOnUiThread(Runnable { setDownloadOverWifiOnly(isWifiOnly) })
                }
            }
        }

        view.runOnUiThread(Runnable {
            downloadDownloadJobLive = appDatabase.downloadJobDao.getJobLive(currentJobId)
            downloadDownloadJobLive.observe(this@DownloadDialogPresenter.context as DoorLifecycleOwner,
                    this@DownloadDialogPresenter::handleDownloadJobStatusChange)
        })
    }

    private fun handleDownloadJobStatusChange(downloadJob: DownloadJob?) {
        val downloadStatus = downloadJob?.djStatus ?: -1
        when {
            downloadStatus >= JobStatus.COMPLETE_MIN && downloadStatus < JobStatus.CANCELED -> {
                deleteFileOptions = true
                view.setStackOptionsVisible(false)
                view.setBottomButtonsVisible(true)
                statusMessage = impl.getString(MessageID.download_state_downloaded,
                        context)
                view.setBottomButtonPositiveText(impl.getString(
                        MessageID.download_delete_btn_label, context))
                view.setBottomButtonNegativeText(impl.getString(
                        MessageID.download_cancel_label, context))
            }

            downloadStatus >= JobStatus.RUNNING_MIN && downloadStatus < JobStatus.COMPLETE_MIN -> {
                deleteFileOptions = false
                view.setStackOptionsVisible(true)
                view.setBottomButtonsVisible(false)
                val optionTexts = listOf(MessageID.pause_download,
                        MessageID.download_cancel_label, MessageID.download_continue_stacked_label)
                        .map {impl.getString(it, context)}.toTypedArray()
                statusMessage = impl.getString(MessageID.download_state_downloading,
                        context)
                view.setStackedOptions(
                        intArrayOf(STACKED_BUTTON_PAUSE, STACKED_BUTTON_CANCEL, STACKED_BUTTON_CONTINUE),
                        optionTexts)
                view.setWifiOnlyOptionVisible(true)
            }

            else -> {
                deleteFileOptions = false
                statusMessage = impl.getString(MessageID.download_state_download,
                        context)
                view.setStackOptionsVisible(false)
                view.setBottomButtonsVisible(true)
                view.setBottomButtonPositiveText(impl.getString(
                        MessageID.download_continue_btn_label, context))
                view.setBottomButtonNegativeText(impl.getString(
                        MessageID.download_cancel_label, context))
                view.setWifiOnlyOptionVisible(true)
            }

        }

        val currentJobSizeTotals = jobSizeTotals.value
        if(currentJobSizeTotals == null && !jobSizeLoading.compareAndSet(true, true)) {
            GlobalScope.launch {
                try {
                    val sizeTotals = if(downloadJob != null) {
                        appDatabase.downloadJobDao.getDownloadSizeInfo(downloadJob.djUid)
                    }else {
                        appDatabaseRepo.contentEntryDao.getRecursiveDownloadTotals(contentEntryUid)
                    }
                    jobSizeTotals.value = sizeTotals
                    updateStatusMessage(sizeTotals)
                }catch(e: Exception) {
                    println(e)
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
            view.runOnUiThread(Runnable { view.setStatusText(currentStatuMessage,
                    downloadTotals.numEntries, UMFileUtil.formatFileSize(downloadTotals.totalSize)) })
        }
    }

    override fun onDownloadJobItemChange(status: DownloadJobItemStatus?, downloadJobUid: Int) {

    }

    private suspend fun createDownloadJobAndRequestPreparation() : Boolean{
        val newDownloadJob = DownloadJob(contentEntryUid, getSystemTimeInMillis())
        newDownloadJob.djDestinationDir = destinationDir
        newDownloadJob.djStatus = JobStatus.NEEDS_PREPARED
        val isWifiOnlyChecked = wifiOnlyChecked.value
        newDownloadJob.meteredNetworkAllowed = !isWifiOnlyChecked
        jobItemManager = networkManagerBle.createNewDownloadJobItemManager(newDownloadJob)
        jobItemManager!!.awaitLoaded()
        currentJobId = jobItemManager!!.downloadJobUid
        downloadJobPreparationRequester(currentJobId, context)
        return currentJobId != 0
    }


    fun handleClickPositive() {
        GlobalScope.launch {
            if(deleteFileOptions) {
                requestDelete(contentEntryUid, context)
            }else {
                if(currentJobId == 0) {
                    createDownloadJobAndRequestPreparation()
                }else {
                    continueDownloading()
                }

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
        when (idClicked) {
            STACKED_BUTTON_PAUSE -> GlobalScope.launch {
                appDatabase.downloadJobDao.updateJobAndItems(currentJobId,
                        JobStatus.PAUSED, JobStatus.PAUSING)
                view.cancelOrPauseDownload(currentJobId.toLong(), false)
            }

            STACKED_BUTTON_CONTINUE -> continueDownloading()

            STACKED_BUTTON_CANCEL -> cancelDownload()
        }

        dismissDialog()
    }


    private fun continueDownloading() {
        GlobalScope.launch {
            appDatabase.downloadJobDao.updateJobAndItems(currentJobId,
                    JobStatus.QUEUED, -1, jobStatusFrom = JobStatus.NOT_QUEUED,
                    jobStatusTo = JobStatus.PAUSED)
        }
    }

    private fun dismissDialog() {
        view.runOnUiThread(Runnable { view.dismissDialog() })
    }

    private fun cancelDownload() {
        GlobalScope.launch {
            appDatabase.downloadJobDao
                    .updateJobAndItems(currentJobId, JobStatus.CANCELED,
                            JobStatus.CANCELLING)
            view.cancelOrPauseDownload(currentJobId.toLong(), true)
        }
    }

    fun handleClickWiFiOnlyOption(wifiOnly: Boolean) {
        wifiOnlyChecked.value = wifiOnly
        if(currentJobId != 0) {
            GlobalScope.launch {
                appDatabase.downloadJobDao.setMeteredConnectionAllowedByJobUidAsync(currentJobId,
                        !wifiOnly)
            }
        }
    }

    fun handleStorageOptionSelection(selectedDir: String) {
        GlobalScope.launch {
            appDatabase.downloadJobDao.updateDestinationDirectoryAsync(
                    currentJobId, selectedDir)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkManagerBle.removeDownloadChangeListener(this)
    }

    companion object {

        const val ARG_CONTENT_ENTRY_UID = "contentEntryUid"

        const val STACKED_BUTTON_PAUSE = 0

        const val STACKED_BUTTON_CANCEL = 1

        const val STACKED_BUTTON_CONTINUE = 2
    }
}
