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
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.port.sharedse.networkmanager.DownloadJobPreparer
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import com.ustadmobile.sharedse.network.DownloadJobItemManager
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.jvm.Volatile

class DownloadDialogPresenter(context: Any, private val networkManagerBle: NetworkManagerBleCommon,
                              arguments: Map<String, String>, view: DownloadDialogView,
                              private var appDatabase: UmAppDatabase, private val appDatabaseRepo: UmAppDatabase)
    : UstadBaseController<DownloadDialogView>(context, arguments, view), OnDownloadJobItemChangeListener {

    private var deleteFileOptions = false

    private var contentEntryUid = 0L

    private lateinit var downloadDownloadJobLive: DoorLiveData<DownloadJob?>

    private var allowedMeteredLive: DoorLiveData<Boolean>? = null

    /**
     * Testing purpose
     */
    @Volatile
    var currentJobId: Int = 0
        private set

    private var impl: UstadMobileSystemImpl? = null

    private var statusMessage: String? = null

    private var destinationDir: String? = null

    private var jobItemManager: DownloadJobItemManager? = null

    private val downloadJobStatus = atomic(0)

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        appDatabase = UmAppDatabase.getInstance(context)

        impl = UstadMobileSystemImpl.instance
        contentEntryUid = arguments[ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0L
        UMLog.l(UMLog.INFO, 420, "Starting download presenter for " +
                "content entry uid: " + contentEntryUid)
        view.setWifiOnlyOptionVisible(false)

        impl!!.getStorageDirs(context, object : UmResultCallback<List<UMStorageDir>>{
            override fun onDone(result: List<UMStorageDir>?) {
                destinationDir = result?.get(0)?.dirURI
                view.runOnUiThread(Runnable{ view.setUpStorageOptions(result!!) })
                GlobalScope.launch {
                    setup()
                }
            }
        })

        networkManagerBle.addDownloadChangeListener(this)
    }


    private fun startObservingJob() {
        view.runOnUiThread(Runnable {
            downloadDownloadJobLive = appDatabase.downloadJobDao.getJobLive(currentJobId)
            downloadDownloadJobLive.observe(this@DownloadDialogPresenter.context as DoorLifecycleOwner,
                    this@DownloadDialogPresenter::handleDownloadJobStatusChange)
        })
    }

    private fun startObservingDownloadJobMeteredState() {
        view.runOnUiThread(Runnable {
            allowedMeteredLive = appDatabase.downloadJobDao
                    .getLiveMeteredNetworkAllowed(currentJobId)
            allowedMeteredLive!!.observe(this@DownloadDialogPresenter.context as DoorLifecycleOwner,
                    this@DownloadDialogPresenter::handleDownloadJobMeteredStateChange)
        })
    }

    private fun handleDownloadJobMeteredStateChange(meteredConnection: Boolean?) {
        view.setDownloadOverWifiOnly(meteredConnection != null && !meteredConnection)
    }

    private fun handleDownloadJobStatusChange(downloadJob: DownloadJob?) {

        if (downloadJob != null) {
            val downloadStatus = downloadJob.djStatus
            downloadJobStatus.value = downloadStatus
            when {
                downloadStatus >= JobStatus.COMPLETE_MIN -> {
                    deleteFileOptions = true
                    view.setStackOptionsVisible(false)
                    view.setBottomButtonsVisible(true)
                    statusMessage = impl!!.getString(MessageID.download_state_downloaded,
                            context)
                    view.setBottomButtonPositiveText(impl!!.getString(
                            MessageID.download_delete_btn_label, context))
                    view.setBottomButtonNegativeText(impl!!.getString(
                            MessageID.download_cancel_label, context))
                }
                downloadStatus >= JobStatus.RUNNING_MIN -> {
                    deleteFileOptions = false
                    view.setStackOptionsVisible(true)
                    view.setBottomButtonsVisible(false)
                    val optionTexts = arrayOf(impl!!.getString(MessageID.pause_download, context),
                            impl!!.getString(MessageID.download_cancel_label, context),
                            impl!!.getString(MessageID.download_continue_stacked_label, context))
                    statusMessage = impl!!.getString(MessageID.download_state_downloading,
                            context)
                    view.setStackedOptions(
                            intArrayOf(STACKED_BUTTON_PAUSE, STACKED_BUTTON_CANCEL, STACKED_BUTTON_CONTINUE),
                            optionTexts)
                }
            }
        }else{
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

        generateStatusMessage()
        view.setCalculatingViewVisible(false)
        view.setWifiOnlyOptionVisible(true)
    }

    private suspend fun setup() {
        currentJobId = appDatabase.downloadJobDao
                .getLatestDownloadJobUidForContentEntryUid(contentEntryUid)

        if (currentJobId != 0) {
            jobItemManager = networkManagerBle.openDownloadJobItemManager(currentJobId)!!
        }

        startObservingJob()
        startObservingDownloadJobMeteredState()
    }

    private fun generateStatusMessage(){
        GlobalScope.launch {
            val umEntriesWithSize = appDatabase.contentEntryParentChildJoinDao
                    .getParentChildContainerRecursiveAsync(contentEntryUid)!!

            view.runOnUiThread(Runnable {
                view.setStatusText(statusMessage ?: "", umEntriesWithSize.numEntries,
                        UMFileUtil.formatFileSize(umEntriesWithSize.fileSize))
            })
        }
    }


    override fun onDownloadJobItemChange(status: DownloadJobItemStatus?, downloadJobUid: Int) {
        println("onDownloadJobItemChange: $status  / $downloadJobUid")
    }

    private suspend fun createDownloadJobRecursive() : Boolean{
        val newDownloadJob = DownloadJob(contentEntryUid, getSystemTimeInMillis())
        newDownloadJob.djDestinationDir = destinationDir
        jobItemManager = networkManagerBle.createNewDownloadJobItemManager(newDownloadJob)
        jobItemManager!!.awaitLoaded()
        currentJobId = jobItemManager!!.downloadJobUid
        DownloadJobPreparer(jobItemManager!!, appDatabase, appDatabaseRepo).run()
        return currentJobId != 0
    }


    fun handleClickPositive() {
        if (deleteFileOptions) {
            GlobalScope.launch {
                networkManagerBle.cancelAndDeleteDownloadJob(currentJobId)
            }
        } else {
            GlobalScope.launch {
                val created = createDownloadJobRecursive()
                if(created){
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
        if (downloadJobStatus.value == 0) {
           GlobalScope.launch {
               networkManagerBle.deleteUnusedDownloadJob(currentJobId)
           }
        }

        //if the download has not been started
        if (dismissAfter)
            dismissDialog()
    }

    fun handleClickStackedButton(idClicked: Int) {
        when (idClicked) {
            STACKED_BUTTON_PAUSE -> GlobalScope.launch {
                appDatabase.downloadJobDao.updateJobAndItems(currentJobId,
                        JobStatus.PAUSED, JobStatus.PAUSING)
            }.start()

            STACKED_BUTTON_CONTINUE -> continueDownloading()

            STACKED_BUTTON_CANCEL -> cancelDownload()
        }

        dismissDialog()
    }


    private fun continueDownloading() {
        GlobalScope.launch {
            appDatabase.downloadJobDao.updateJobAndItems(currentJobId,
                    JobStatus.QUEUED, -1)
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
        }.start()
    }

    fun handleWiFiOnlyOption(wifiOnly: Boolean) {
        GlobalScope.launch {
            appDatabase.downloadJobDao.setMeteredConnectionAllowedByJobUidAsync(currentJobId,
                    !wifiOnly)
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
