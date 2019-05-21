package com.ustadmobile.port.sharedse.controller

import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.DownloadJobItemManager
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import com.ustadmobile.port.sharedse.networkmanager.DownloadJobPreparer
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import java.util.concurrent.atomic.AtomicInteger

class DownloadDialogPresenter(context: Any, private val networkManagerBle: NetworkManagerBle,
                              arguments: Map<String, String>, view: DownloadDialogView,
                              private var appDatabase: UmAppDatabase?, private val appDatabaseRepo: UmAppDatabase) : UstadBaseController<DownloadDialogView>(context, arguments, view) {

    private var deleteFileOptions = false

    private var contentEntryUid = 0L

    private var downloadDownloadJobLive: UmLiveData<DownloadJob>? = null

    private var allowedMeteredLive: UmLiveData<Boolean>? = null

    /**
     * Testing purpose
     */
    @Volatile
    var currentJobId = 0L
        private set

    private var impl: UstadMobileSystemImpl? = null

    private var statusMessage: String? = null

    private var destinationDir: String? = null

    private var jobItemManager: DownloadJobItemManager? = null

    private val downloadJobStatus = AtomicInteger(0)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        appDatabase = UmAppDatabase.getInstance(context)

        impl = UstadMobileSystemImpl.instance
        contentEntryUid = java.lang.Long.parseLong(arguments[ARG_CONTENT_ENTRY_UID].toString())
        UMLog.l(UMLog.INFO, 420, "Starting download presenter for " +
                "content entry uid: " + contentEntryUid)
        view.setWifiOnlyOptionVisible(false)

        impl!!.getStorageDirs(context, { result ->
            destinationDir = result.get(0).dirURI
            view.runOnUiThread({ view.setUpStorageOptions(result) })
            Thread(Runnable { this.setup() }).start()
        })

    }


    private fun startObservingJob() {
        view.runOnUiThread({
            downloadDownloadJobLive = appDatabase!!.downloadJobDao.getJobLive(currentJobId)
            downloadDownloadJobLive!!.observe(this@DownloadDialogPresenter,
                    UmObserver<DownloadJob> { this.handleDownloadJobStatusChange(it) })
        })
    }

    private fun startObservingDownloadJobMeteredState() {
        view.runOnUiThread({
            allowedMeteredLive = appDatabase!!.downloadJobDao
                    .getLiveMeteredNetworkAllowed(currentJobId.toInt())
            allowedMeteredLive!!.observe(this@DownloadDialogPresenter,
                    UmObserver<Boolean> { this.handleDownloadJobMeteredStateChange(it) })
        })
    }

    private fun handleDownloadJobMeteredStateChange(meteredConnection: Boolean?) {
        view.setDownloadOverWifiOnly(meteredConnection != null && !meteredConnection)
    }

    private fun handleDownloadJobStatusChange(downloadJob: DownloadJob?) {
        if (downloadJob != null) {
            val downloadStatus = downloadJob.djStatus
            downloadJobStatus.set(downloadStatus)
            view.setCalculatingViewVisible(false)
            view.setWifiOnlyOptionVisible(true)
            if (downloadStatus >= JobStatus.COMPLETE_MIN) {
                deleteFileOptions = true
                view.setStackOptionsVisible(false)
                view.setBottomButtonsVisible(true)
                statusMessage = impl!!.getString(MessageID.download_state_downloaded,
                        context)
                view.setBottomButtonPositiveText(impl!!.getString(
                        MessageID.download_delete_btn_label, context))
                view.setBottomButtonNegativeText(impl!!.getString(
                        MessageID.download_cancel_label, context))
            } else if (downloadStatus >= JobStatus.RUNNING_MIN) {
                deleteFileOptions = false
                view.setStackOptionsVisible(true)
                view.setBottomButtonsVisible(false)
                val optionTexts = arrayOf(impl!!.getString(MessageID.download_pause_stacked_label, context), impl!!.getString(MessageID.download_cancel_stacked_label, context), impl!!.getString(MessageID.download_continue_stacked_label, context))
                statusMessage = impl!!.getString(MessageID.download_state_downloading,
                        context)
                view.setStackedOptions(
                        intArrayOf(STACKED_BUTTON_PAUSE, STACKED_BUTTON_CANCEL, STACKED_BUTTON_CONTINUE),
                        optionTexts)
            } else {
                deleteFileOptions = false
                statusMessage = impl!!.getString(MessageID.download_state_download,
                        context)
                view.setStackOptionsVisible(false)
                view.setBottomButtonsVisible(true)
                view.setBottomButtonPositiveText(impl!!.getString(
                        MessageID.download_continue_btn_label, context))
                view.setBottomButtonNegativeText(impl!!.getString(
                        MessageID.download_cancel_label, context))
            }


            Thread {
                val totalDownloadJobItems = appDatabase!!.downloadJobItemDao
                        .getTotalDownloadJobItems(currentJobId)
                val rootStatus = jobItemManager!!.getRootItemStatus()
                view.runOnUiThread({
                    view.setStatusText(statusMessage, totalDownloadJobItems,
                            UMFileUtil.formatFileSize(if (rootStatus != null) rootStatus!!.totalBytes else 0))
                })
            }.start()

        }
    }

    private fun setup() {
        currentJobId = appDatabase!!.downloadJobDao
                .getLatestDownloadJobUidForContentEntryUid(contentEntryUid)
        if (currentJobId == 0L) {
            createDownloadJobRecursive()
        }

        startObservingJob()

        startObservingDownloadJobMeteredState()

    }

    private fun createDownloadJobRecursive() {
        val newDownloadJob = DownloadJob(contentEntryUid, System.currentTimeMillis())
        newDownloadJob.djDestinationDir = destinationDir
        jobItemManager = networkManagerBle.createNewDownloadJobItemManager(newDownloadJob)
        currentJobId = jobItemManager!!.getDownloadJobUid()
        DownloadJobPreparer(jobItemManager, appDatabase, appDatabaseRepo).run()
    }


    fun handleClickPositive() {
        if (deleteFileOptions) {
            Thread { networkManagerBle.cancelAndDeleteDownloadJob(currentJobId.toInt()) }.start()
        } else {
            continueDownloading()
        }
    }

    /**
     * Handle negative click. If the underlying system is already dismissing the dialog
     * set dismissAfter to false to avoid a call to dismissDialog
     * @param dismissAfter flag to indicate if the dialog will be dismissed after the selection
     */
    @JvmOverloads
    fun handleClickNegative(dismissAfter: Boolean = true) {
        if (downloadJobStatus.get() == 0) {
            Thread { networkManagerBle.deleteUnusedDownloadJob(currentJobId.toInt()) }.start()
        }

        //if the download has not been started
        if (dismissAfter)
            dismissDialog()
    }

    fun handleClickStackedButton(idClicked: Int) {
        when (idClicked) {
            STACKED_BUTTON_PAUSE -> Thread {
                appDatabase!!.downloadJobDao.updateJobAndItems(currentJobId,
                        JobStatus.PAUSED, JobStatus.PAUSING)
            }.start()

            STACKED_BUTTON_CONTINUE -> continueDownloading()

            STACKED_BUTTON_CANCEL -> cancelDownload()
        }

        dismissDialog()
    }


    private fun continueDownloading() {
        Thread {
            appDatabase!!.downloadJobDao.updateJobAndItems(currentJobId,
                    JobStatus.QUEUED, -1)
        }.start()
    }

    private fun dismissDialog() {
        view.runOnUiThread({ view.dismissDialog() })
    }

    private fun cancelDownload() {
        Thread {
            appDatabase!!.downloadJobDao
                    .updateJobAndItems(currentJobId, JobStatus.CANCELED,
                            JobStatus.CANCELLING)
        }.start()
    }

    fun handleWiFiOnlyOption(wifiOnly: Boolean) {
        appDatabase!!.downloadJobDao.setMeteredConnectionAllowedByJobUid(currentJobId.toInt(),
                !wifiOnly, null)
    }

    fun handleStorageOptionSelection(selectedDir: String) {
        appDatabase!!.downloadJobDao.updateDestinationDirectory(
                currentJobId.toInt(), selectedDir, null)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {

        val ARG_CONTENT_ENTRY_UID = "contentEntryUid"

        val STACKED_BUTTON_PAUSE = 0

        val STACKED_BUTTON_CANCEL = 1

        val STACKED_BUTTON_CONTINUE = 2
    }
}
