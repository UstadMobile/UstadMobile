package com.ustadmobile.sharedse.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.catalog.contenttype.ContentEntryBranchDownloadPlugin.Companion.CONTENT_ENTRY_BRANCH_DOWNLOAD_PLUGIN_ID
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ContentPluginIds.CONTAINER_DOWNLOAD_PLUGIN
import com.ustadmobile.core.contentjob.ContentPluginIds.DELETE_CONTENT_ENTRY_PLUGIN
import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.RateLimitedLiveData
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.isStatusActiveOrQueued
import com.ustadmobile.core.util.ext.toDeepLink
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.*
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.sharedse.view.DownloadDialogView
import io.github.aakira.napier.Napier
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.lifecycle.Observer


class DownloadDialogPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: DownloadDialogView,
    di: DI,
    private val lifecycleOwner: LifecycleOwner
) : UstadBaseController<DownloadDialogView>(context, arguments, view, di), Observer<Int?> {

    private var deleteFileOptions = false

    private var contentEntryUid = 0L

    private var currentJobId: Long = 0L

    private var statusMessage: String? = null

    private val jobSizeLoading = atomic(false)

    private val jobSizeTotals = atomic(null as DownloadJobSizeInfo?)

    private val wifiOnlyChecked = atomic(true)

    private lateinit var contentJobItemStatusLiveData: RateLimitedLiveData<Int>

    private var currentContentJobItemStatus: Int = 0

    //Used to avoid issues with handleStorageOptionSelection being called before the
    // downloadJobItemLiveData is ready.
    private val contentJobCompletable = CompletableDeferred<Boolean>()

    private var selectedStorageDir: ContainerStorageDir? = null

    private val accountManager: UstadAccountManager by instance()

    private val impl: UstadMobileSystemImpl by instance()

    private val contentJobManager: ContentJobManager by di.instance()

    private val appDatabase: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_DB)

    private val appDatabaseRepo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        contentEntryUid = arguments[UstadView.ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0L
        Napier.i("Starting download presenter for $contentEntryUid")
        view.setWifiOnlyOptionVisible(false)
        GlobalScope.launch(doorMainDispatcher()) {
            contentJobItemStatusLiveData = RateLimitedLiveData(appDatabase,
                listOf("ContentJobItem"), 1000
            ) {
                appDatabase.contentEntryDao.statusForDownloadDialog(contentEntryUid)
            }

            currentJobId = appDatabase.contentJobItemDao
                .getActiveContentJobIdByContentEntryUid(contentEntryUid)
            contentJobCompletable.complete(true)

            val status = contentJobItemStatusLiveData.getValue() ?: 0

            val wifiOnly = !appDatabase.contentEntryDao.isMeteredAllowedForEntry(contentEntryUid)
            view.setDownloadOverWifiOnly(wifiOnly)

            view.runOnUiThread(Runnable {
                // atomic doesn't like globalScope
                wifiOnlyChecked.value = wifiOnly
                contentJobItemStatusLiveData.observe(lifecycleOwner, this@DownloadDialogPresenter)
            })

            updateWarningMessage(status)
        }
    }

    override fun onChanged(t: Int?) {
        currentContentJobItemStatus = t ?: 0
        when{

            currentContentJobItemStatus == JobStatus.COMPLETE -> {
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
            currentContentJobItemStatus.isStatusActiveOrQueued() -> {
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
        if(currentContentJobItemStatus != JobStatus.RUNNING && currentJobSizeTotals == null
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
            cjIsMeteredAllowed = !isWifiOnlyChecked
            cjNotificationTitle = impl.getString(MessageID.downloading_content, context)
                    .replace("%1\$s",entry?.title ?: "")
            cjUid = appDatabase.contentJobDao.insertAsync(this)
        }
        currentJobId = job.cjUid

        ContentJobItem().apply {
            cjiJobUid = job.cjUid
            sourceUri = entry?.toDeepLink(accountManager.activeEndpoint)
            cjiItemTotal = container?.fileSize ?: 0
            cjiPluginId = if(entry?.leaf == true) {
                CONTAINER_DOWNLOAD_PLUGIN
            } else {
                CONTENT_ENTRY_BRANCH_DOWNLOAD_PLUGIN_ID
            }
            cjiContentEntryUid = contentEntryUid
            cjiContainerUid = container?.containerUid ?: 0
            cjiIsLeaf = entry?.leaf ?: false
            cjiConnectivityNeeded = true
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
            JobStatus.COMPLETE -> GlobalScope.launch {
                createDeleteJob()
            }
            else -> GlobalScope.launch {
                createDownloadJob()
            }
        }
    }

    private suspend fun createDeleteJob() {
        val entry = appDatabaseRepo.contentEntryDao.findByUid(contentEntryUid)
        val container = appDatabaseRepo.containerDao
                .getMostRecentDownloadedContainerForContentEntryAsync(contentEntryUid)
        val job = ContentJob().apply {
            cjNotificationTitle = impl.getString(MessageID.deleting_content, context)
                    .replace("%1\$s",entry?.title ?: "")
            cjUid = appDatabase.contentJobDao.insertAsync(this)
        }
        currentJobId = job.cjUid
        ContentJobItem().apply {
            cjiJobUid = job.cjUid
            sourceUri = entry?.toDeepLink(accountManager.activeEndpoint)
            cjiPluginId = DELETE_CONTENT_ENTRY_PLUGIN
            cjiContainerUid = container?.containerUid ?: 0
            cjiContentEntryUid = entry?.contentEntryUid ?: 0
            cjiIsLeaf = true
            cjiItemTotal = 100
            cjiParentContentEntryUid = 0
            cjiConnectivityNeeded = false
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
                STACKED_BUTTON_CANCEL -> GlobalScope.launch {
                    createCancelJob()
                }
            }
            dismissDialog()
    }

    private suspend fun createCancelJob(){
        contentJobManager.cancelContentJob(accountManager.activeEndpoint, currentJobId)
    }

    private fun dismissDialog() {
        view.runOnUiThread(Runnable { view.dismissDialog() })
    }

    fun handleClickWiFiOnlyOption(wifiOnly: Boolean) {
        wifiOnlyChecked.value = wifiOnly
        GlobalScope.launch(doorMainDispatcher()){
            appDatabase.contentJobDao.updateMeteredAllowedForEntry(contentEntryUid, !wifiOnly)
        }
    }

    fun handleStorageOptionSelection(selectedDir: ContainerStorageDir) {
        selectedStorageDir = selectedDir
        GlobalScope.launch(doorMainDispatcher()) {
            contentJobCompletable.await()
            updateWarningMessage(contentJobItemStatusLiveData.getValue() ?: 0)
        }
    }

    companion object {

        const val STACKED_BUTTON_CANCEL = 0

        val STACKED_OPTIONS = intArrayOf(STACKED_BUTTON_CANCEL)

        val STACKED_TEXT_MESSAGE_IDS = listOf(
                MessageID.download_cancel_label)
    }
}
