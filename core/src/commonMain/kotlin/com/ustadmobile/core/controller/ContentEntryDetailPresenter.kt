package com.ustadmobile.core.controller

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.ARG_REFERRER
import com.ustadmobile.core.networkmanager.DownloadJobItemStatusProvider
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor
import com.ustadmobile.core.networkmanager.OnDownloadJobItemChangeListener
import com.ustadmobile.core.util.ContentEntryUtil
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_CREATE_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_IMPORT_FILE
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryStatus
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.js.JsName

class ContentEntryDetailPresenter(context: Any, arguments: Map<String, String?>,
                                  viewContract: ContentEntryDetailView,
                                  private val monitor: LocalAvailabilityMonitor,
                                  private val statusProvider: DownloadJobItemStatusProvider?,
                                  private val appRepo: UmAppDatabase)
    : UstadBaseController<ContentEntryDetailView>(context, arguments, viewContract),
        OnDownloadJobItemChangeListener {

    private var navigation: String? = null

    var entryUuid: Long = 0
        private set

    private var containerUid: Long? = 0L

    private val monitorStatus = atomic(false)

    private val args = HashMap<String, String?>()

    private val isListeningToDownloadStatus = atomic(false)

    private var statusUmLiveData: DoorLiveData<ContentEntryStatus?>? = null

    private val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

    private var entryLiveData: DoorLiveData<ContentEntry?>? = null

    private var isDownloadComplete: Boolean = false

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)



        entryUuid = arguments.getValue(ARG_CONTENT_ENTRY_UID)!!.toLong()
        navigation = arguments[ARG_REFERRER]

        entryLiveData = appRepo.contentEntryDao.findLiveContentEntry(entryUuid)
        entryLiveData!!.observe(this, this::onEntryChanged)

        GlobalScope.launch {
            val result = appRepo.containerDao.findFilesByContentEntryUid(entryUuid)
            view.runOnUiThread(Runnable {
                view.setDetailsButtonEnabled(result.isNotEmpty())
                if (result.isNotEmpty()) {
                    val container = result[0]
                    view.setDownloadSize(container.fileSize)
                }
            })
        }

        GlobalScope.launch {
            val result = appRepo.contentEntryRelatedEntryJoinDao.findAllTranslationsForContentEntryAsync(entryUuid)
            view.runOnUiThread(Runnable {
                view.setTranslationLabelVisible(result.isNotEmpty())
                view.setFlexBoxVisible(result.isNotEmpty())
                view.setAvailableTranslations(result, entryUuid)
            })
        }

        statusUmLiveData = appRepo.contentEntryStatusDao.findContentEntryStatusByUid(entryUuid)

        statusUmLiveData!!.observe(this, this::onEntryStatusChanged)

        statusProvider?.addDownloadChangeListener(this)
    }

    private fun onEntryChanged(entry: ContentEntry?) {
        if (entry != null) {
            val licenseType = getLicenseType(entry)
            view.runOnUiThread(Runnable {
                view.setContentEntryLicense(licenseType)
                with(entry) {
                    view.setContentEntry(this)
                }
            })
        }
    }

    private fun getLicenseType(result: ContentEntry): String {
        when (result.licenseType) {
            ContentEntry.LICENSE_TYPE_CC_BY -> return "CC BY"
            ContentEntry.LICENSE_TYPE_CC_BY_SA -> return "CC BY SA"
            ContentEntry.LICENSE_TYPE_CC_BY_SA_NC -> return "CC BY SA NC"
            ContentEntry.LICENSE_TYPE_CC_BY_NC -> return "CC BY NC"
            ContentEntry.LICESNE_TYPE_CC_BY_NC_SA -> return "CC BY NC SA"
            ContentEntry.PUBLIC_DOMAIN -> return "Public Domain"
            ContentEntry.ALL_RIGHTS_RESERVED -> return "All Rights Reserved"
        }
        return ""
    }


    private fun onEntryStatusChanged(status: ContentEntryStatus?) {

        isDownloadComplete = status != null && status.downloadStatus == JobStatus.COMPLETE

        val buttonLabel = impl.getString(if (status == null || !isDownloadComplete)
            MessageID.download
        else
            MessageID.open, context)

        val progressLabel = impl.getString(MessageID.downloading, context)

        val isDownloading = (status != null
                && status.downloadStatus >= JobStatus.RUNNING_MIN
                && status.downloadStatus <= JobStatus.RUNNING_MAX)

        if (isDownloading && isListeningToDownloadStatus.value) {
            isListeningToDownloadStatus.value = true
            GlobalScope.launch {
                val dlJobStatusResult = statusProvider?.findDownloadJobItemStatusByContentEntryUid(entryUuid)
                onDownloadJobItemChange(dlJobStatusResult, dlJobStatusResult?.jobItemUid ?: 0)
            }

            view.setDownloadButtonVisible(false)
            view.setDownloadProgressVisible(true)
        } else if (!isDownloading && isListeningToDownloadStatus.value) {
            isListeningToDownloadStatus.value = false
            statusProvider?.removeDownloadChangeListener(this)
            view.setDownloadButtonVisible(true)
            view.setDownloadProgressVisible(false)
        }

        view.runOnUiThread(Runnable {
            view.setButtonTextLabel(buttonLabel)
            view.setDownloadButtonVisible(!isDownloading)
            view.setDownloadProgressVisible(isDownloading)
            view.setDownloadProgressLabel(progressLabel)
            view.setLocalAvailabilityStatusViewVisible(isDownloading)
        })

        if (isDownloading) {
            view.runOnUiThread(Runnable {
                view.setDownloadButtonVisible(false)
                view.setDownloadProgressVisible(true)
                view.updateDownloadProgress(if (status!!.totalSize > 0)
                    status.bytesDownloadSoFar.toFloat() / status.totalSize.toFloat()
                else 0f)
            })

        }


        if (!isDownloadComplete) {
            val currentTimeStamp = getSystemTimeInMillis()
            val minLastSeen = currentTimeStamp - 60000
            val maxFailureFromTimeStamp = currentTimeStamp - 300000

            GlobalScope.launch {

                val container = appRepo.containerDao.getMostRecentContainerForContentEntry(entryUuid)
                if (container != null) {
                    containerUid = container.containerUid
                    val localNetworkNode = appRepo.networkNodeDao.findLocalActiveNodeByContainerUid(
                            containerUid!!, minLastSeen, BAD_NODE_FAILURE_THRESHOLD, maxFailureFromTimeStamp)

                    if (localNetworkNode == null && !monitorStatus.value) {
                        monitorStatus.value = true
                        monitor.startMonitoringAvailability(this,
                                listOf(containerUid!!))
                    }

                    val monitorSet: MutableSet<Long> = view.allKnowAvailabilityStatus as MutableSet<Long>

                    monitorSet.add((if (localNetworkNode != null) containerUid else 0L)!!)

                    handleLocalAvailabilityStatus(monitorSet)
                }


            }
        }


    }

    override fun onDownloadJobItemChange(status: DownloadJobItemStatus?, downloadJobUid: Int) {
        if (status != null && status.contentEntryUid == entryUuid) {
            view.runOnUiThread(Runnable {
                view.updateDownloadProgress(
                        if (status.totalBytes > 0) (status.bytesSoFar.toFloat() / status.totalBytes.toFloat()) else 0F)
            })
        }
    }

    @JsName("handleClickTranslatedEntry")
    fun handleClickTranslatedEntry(uid: Long) {
        val args = HashMap<String, String>()
        args[ARG_CONTENT_ENTRY_UID] = uid.toString()
        impl.go(ContentEntryDetailView.VIEW_NAME, args, view.viewContext)
    }

    @JsName("handleUpNavigation")
    fun handleUpNavigation() {
        val lastEntryListArgs = UMFileUtil.getLastReferrerArgsByViewname(ContentEntryListFragmentView.VIEW_NAME, navigation!!)
        if (lastEntryListArgs != null) {
            impl.go(ContentEntryListFragmentView.VIEW_NAME,
                    UMFileUtil.parseURLQueryString(lastEntryListArgs), context,
                    UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP)
        } else {
            impl.go(HomeView.VIEW_NAME, mutableMapOf(), context,
                    UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP)
        }
    }

    fun handleDownloadButtonClick() {
        if (isDownloadComplete) {

            val loginFirst = impl.getAppConfigString(AppConfig.KEY_LOGIN_REQUIRED_FOR_CONTENT_OPEN,
                    "false",context)!!.toBoolean()

            if(loginFirst){
                impl.go(LoginView.VIEW_NAME, args, view.viewContext)
            }else{
                ContentEntryUtil.goToContentEntry(entryUuid, appRepo, impl, isDownloadComplete,
                        context, object : UmCallback<Any> {

                    override fun onSuccess(result: Any?) {}

                    override fun onFailure(exception: Throwable?) {
                        if (exception != null) {
                            val message = exception.message
                            if (exception is NoAppFoundException) {
                                view.runOnUiThread(Runnable {
                                    view.showFileOpenError(impl.getString(MessageID.no_app_found, context),
                                            MessageID.get_app,
                                            exception.mimeType!!)
                                })
                            } else {
                                view.runOnUiThread(Runnable { view.showFileOpenError(message!!) })
                            }
                        }
                    }
                })
            }


        } else {
            val args = HashMap<String, String>()

            //hard coded strings because these are actually in sharedse
            args["contentEntryUid"] = this.entryUuid.toString()
            view.runOnUiThread(Runnable { view.showDownloadOptionsDialog(args) })
        }

    }


    fun handleLocalAvailabilityStatus(locallyAvailableEntries: Set<Long>) {
        val icon = if (locallyAvailableEntries.contains(
                        containerUid))
            LOCALLY_AVAILABLE_ICON
        else
            LOCALLY_NOT_AVAILABLE_ICON

        val status = impl.getString(
                if (icon == LOCALLY_AVAILABLE_ICON)
                    MessageID.download_locally_availability
                else
                    MessageID.download_cloud_availability, context)

        view.runOnUiThread(Runnable { view.updateLocalAvailabilityViews(icon, status) })
    }

    fun handleShowEditButton(show: Boolean){
        view.runOnUiThread(Runnable { view.showEditButton(show)})
    }

    suspend fun handleCancelDownload(){
        val currentJobId = appRepo.downloadJobDao.getLatestDownloadJobUidForContentEntryUid(entryUuid)
                appRepo.downloadJobDao.updateJobAndItems(currentJobId, JobStatus.CANCELED,
                        JobStatus.CANCELLING)
                        appRepo.contentEntryStatusDao.updateDownloadStatus(entryUuid, JobStatus.CANCELED)
        statusProvider?.removeDownloadChangeListener(this)
        view.stopForeGroundService(currentJobId.toLong(), true)
    }


    fun handleStartEditingContent() {

        GlobalScope.launch {
            val entry = appRepo.contentEntryDao.findByEntryId(entryUuid)

            if (entry != null) {
                args.putAll(arguments)
                args[ContentEditorView.CONTENT_ENTRY_UID] = entryUuid.toString()
                args[ContentEntryEditView.CONTENT_ENTRY_LEAF] = true.toString()
                args[ContentEditorView.CONTENT_STORAGE_OPTION] = ""
                args[ContentEntryEditView.CONTENT_TYPE] = (if (entry.imported) CONTENT_IMPORT_FILE
                else CONTENT_CREATE_CONTENT).toString()

                if (entry.imported)
                    view.startFileBrowser(args)
                else
                    impl.go(ContentEditorView.VIEW_NAME, args, context)
            }
        }
    }


    override fun onDestroy() {
        if (monitorStatus.value) {
            monitorStatus.value = false
            monitor.stopMonitoringAvailability(this)
        }

        if (isListeningToDownloadStatus.getAndSet(false)) {
            statusProvider?.removeDownloadChangeListener(this)
        }
        super.onDestroy()
    }

    companion object {

        const val ARG_CONTENT_ENTRY_UID = "entryid"

        const val LOCALLY_AVAILABLE_ICON = 1

        const val LOCALLY_NOT_AVAILABLE_ICON = 2

        private const val BAD_NODE_FAILURE_THRESHOLD = 3
    }

}
