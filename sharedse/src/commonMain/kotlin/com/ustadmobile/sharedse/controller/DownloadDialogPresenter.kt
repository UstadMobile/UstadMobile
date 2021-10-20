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
import com.ustadmobile.core.util.RateLimitedLiveData
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import com.ustadmobile.door.*
import com.ustadmobile.lib.db.entities.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on


class DownloadDialogPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: DownloadDialogView,
    di: DI,
    private val lifecycleOwner: DoorLifecycleOwner
) : UstadBaseController<DownloadDialogView>(context, arguments, view, di), DoorObserver<Int?> {

    private var deleteFileOptions = false

    private var contentEntryUid = 0L

    private var statusMessage: String? = null

    private val jobSizeLoading = atomic(false)

    private val jobSizeTotals = atomic(null as DownloadJobSizeInfo?)

    private val wifiOnlyChecked = atomic(0)

    private lateinit var contentJobItemStatusLiveData: RateLimitedLiveData<Int>

    private var currentContentJobItemStatus: Int = 0

    //Used to avoid issues with handleStorageOptionSelection being called before the
    // downloadJobItemLiveData is ready.
    private val contentJobCompletable = CompletableDeferred<Boolean>()

    private var selectedStorageDir: ContainerStorageDir? = null

    private val accountManager: UstadAccountManager by instance()

    private val impl: UstadMobileSystemImpl by instance()

    private val contentJobManager: ContentJobManager by di.instance()

    private val appDatabase: UmAppDatabase by on(accountManager.activeAccount).instance(tag = TAG_DB)

    private val appDatabaseRepo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = TAG_REPO)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        contentEntryUid = arguments[UstadView.ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0L
        Napier.i("Starting download presenter for $contentEntryUid")
        view.setWifiOnlyOptionVisible(false)
        GlobalScope.launch {
            contentJobItemStatusLiveData = RateLimitedLiveData(appDatabase, listOf("ContentJobItem"), 1000) {
                appDatabase.contentJobItemDao.findStatusForActiveContentJobItem(contentEntryUid)
            }
            contentJobCompletable.complete(true)
            val status = contentJobItemStatusLiveData.getValue() ?: 0
            val connectivityAcceptable = appDatabase.contentEntryDao.getConnectivityAcceptableForEntry(contentEntryUid)
            val wifiOnly: Boolean = connectivityAcceptable == 0 || connectivityAcceptable == ContentJobItem.ACCEPT_METERED
            view.setDownloadOverWifiOnly(wifiOnly)
            val wifiCheckValue = if(connectivityAcceptable == 0) ContentJobItem.ACCEPT_METERED else connectivityAcceptable
            wifiOnlyChecked.value = wifiCheckValue

            view.runOnUiThread(Runnable {
                contentJobItemStatusLiveData.observe(lifecycleOwner, this@DownloadDialogPresenter)
            })

            updateWarningMessage(status)
        }
    }

    override fun onChanged(t: Int?) {
        currentContentJobItemStatus = t ?: 0
        when{

            currentContentJobItemStatus == ContentJobItem.STATUS_COMPLETE -> {
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
            currentContentJobItemStatus == ContentJobItem.STATUS_RUNNING -> {
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
        if(currentContentJobItemStatus != ContentJobItem.STATUS_RUNNING && currentJobSizeTotals == null
                && !jobSizeLoading.compareAndSet(expect = true, update = true)) {
            view.setBottomPositiveButtonEnabled(false)
            GlobalScope.launch {
                try {
                    val sizeTotals = appDatabaseRepo.contentEntryDao.getRecursiveDownloadTotals(contentEntryUid)
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
                updateWarningMessage(contentJobItemStatusLiveData.getValue() ?: 0)
                view.setCalculatingViewVisible(false)
                view.setStatusText(currentStatuMessage,
                    downloadTotals.numEntries, UMFileUtil.formatFileSize(downloadTotals.totalSize))
            })
        }
    }

    private fun updateWarningMessage(status: Int) {
        val jobSizeTotalsVal = jobSizeTotals.value
        val selectedStorageDirVal = selectedStorageDir
        if((status <= JobStatus.PAUSED)  && jobSizeTotalsVal != null
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

        return job.cjUid != 0L
    }


    /**
     * The positive button can be either the download button or the delete button
     */
    fun handleClickPositive() {
        when (currentContentJobItemStatus) {
            ContentJobItem.STATUS_COMPLETE -> GlobalScope.launch {
                createDeleteJob()
            }
            JobStatus.PAUSED -> GlobalScope.launch {
                // TODO handle paused state
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
            cjiItemTotal = 100
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

    private fun dismissDialog() {
        view.runOnUiThread(Runnable { view.dismissDialog() })
    }

    fun handleClickWiFiOnlyOption(wifiOnly: Boolean) {
        val wifiOnlyCheckedVal = if(wifiOnly) ContentJobItem.ACCEPT_METERED else ContentJobItem.ACCEPT_UNMETERED
        wifiOnlyChecked.value = wifiOnlyCheckedVal
      /*  if(currentJobId != 0L) {
            GlobalScope.launch {
                appDatabase.contentJobItemDao.updateConnectivityStatus(currentJobId, wifiOnlyCheckedVal)
            }
        }*/
    }

    fun handleStorageOptionSelection(selectedDir: ContainerStorageDir) {
        selectedStorageDir = selectedDir
        GlobalScope.launch(doorMainDispatcher()) {
            contentJobCompletable.await()
            updateWarningMessage(contentJobItemStatusLiveData.getValue() ?: 0)
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
