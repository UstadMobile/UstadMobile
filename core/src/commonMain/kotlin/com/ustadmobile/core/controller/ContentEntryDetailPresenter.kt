package com.ustadmobile.core.controller

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.ARG_REFERRER
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.GoToEntryFn
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.isStatusCompletedSuccessfully
import com.ustadmobile.core.util.goToContentEntry
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.liveDataObserverDispatcher
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.FLAG_CONTENT_EDITOR
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.FLAG_IMPORTED
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.js.JsName


// the view should handle this being empty
class ContentEntryDetailPresenter(context: Any, arguments: Map<String, String?>,
                                  viewContract: ContentEntryDetailView,
                                  private val isDownloadEnabled: Boolean,
                                  private val appRepo: UmAppDatabase,
                                  private val appDb: UmAppDatabase,
                                  private val localAvailabilityManager: LocalAvailabilityManager?,
                                  private val containerDownloadManager: ContainerDownloadManager?,
                                  private val activeAccount: UmAccount?,
                                  private val impl: UstadMobileSystemImpl,
                                  private val goToEntryFn: GoToEntryFn = ::goToContentEntry)
    : UstadBaseController<ContentEntryDetailView>(context, arguments, viewContract) {

    private var navigation: String? = null

    var entryUuid: Long = 0
        private set

    private var containerUid: Long? = 0L

    private val args = HashMap<String, String?>()

    private lateinit var entryLiveData: DoorLiveData<ContentEntry?>

    private var showEditorControls: Boolean = false

    private var currentContentEntry: ContentEntry = ContentEntry()

    private var availabilityMonitorRequest: AvailabilityMonitorRequest? = null

    private var downloadJobItemLiveData: DoorLiveData<DownloadJobItem?>? = null

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        entryUuid = arguments.getValue(ARG_CONTENT_ENTRY_UID)!!.toLong()
        navigation = arguments[ARG_REFERRER]
        entryLiveData = appRepo.contentEntryDao.findLiveContentEntry(entryUuid)
        entryLiveData.observe(this, ::onEntryChanged)

        GlobalScope.launch {
            val result = appDb.containerDao.getMostRecentContainerForContentEntryAsync(entryUuid)
            view.runOnUiThread(Runnable {
                view.setMainButtonEnabled(result != null)
                view.setDownloadSize(result?.fileSize ?: 0)
            })
        }

        if (containerDownloadManager != null) {
            GlobalScope.launch(liveDataObserverDispatcher()) {
                downloadJobItemLiveData = containerDownloadManager.getDownloadJobItemByContentEntryUid(entryUuid).also {
                    it.observe(this@ContentEntryDetailPresenter, this@ContentEntryDetailPresenter::onDownloadJobItemChanged)
                }
            }
        } else {
            view.setDownloadJobItemStatus(null)
        }


        GlobalScope.launch {
            view.showBaseProgressBar(true)
            val result = appRepo.contentEntryRelatedEntryJoinDao.findAllTranslationsForContentEntryAsync(entryUuid)
            view.runOnUiThread(Runnable {
                view.setAvailableTranslations(result)
                view.showBaseProgressBar(false)
            })
        }

//        statusUmLiveData = appRepo.contentEntryStatusDao.findContentEntryStatusByUid(entryUuid)
//
//        statusUmLiveData!!.observe(this, this::onEntryStatusChanged)

        //statusProvider?.addDownloadChangeListener(this)
    }

    private fun onEntryChanged(entry: ContentEntry?) {
        if (entry != null) {
            val licenseType = getLicenseType(entry.licenseType)

            if (currentContentEntry != entry) {
                currentContentEntry = entry
                view.setContentEntryLicense(licenseType)
                GlobalScope.launch {
                    with(entry) {
                        val canShowEditBtn = (((this.contentFlags and FLAG_CONTENT_EDITOR) == FLAG_CONTENT_EDITOR)
                                || (this.contentFlags and FLAG_IMPORTED) == FLAG_IMPORTED)
                        if (activeAccount != null) {
                            val person = appRepo.personDao.findByUid(activeAccount.personUid)
                            if (person != null) {
                                view.runOnUiThread(Runnable {
                                    view.setEditButtonVisible(person.admin && showEditorControls && canShowEditBtn)
                                })
                            }
                        }
                    }
                }
                view.setContentEntry(entry)
            }

        }
    }

    private fun onDownloadJobItemChanged(downloadJobItem: DownloadJobItem?) {
        view.setDownloadJobItemStatus(downloadJobItem)

        if (availabilityMonitorRequest == null && !downloadJobItem.isStatusCompletedSuccessfully()) {
            GlobalScope.launch {
                val container = appRepo.containerDao.getMostRecentContainerForContentEntry(entryUuid)
                if (container != null) {
                    containerUid = container.containerUid
                    val containerUidList = listOf(containerUid!!)
                    val availableNowMap = localAvailabilityManager?.areContentEntriesLocallyAvailable(containerUidList)
                            ?: mapOf()

                    view.runOnUiThread(Runnable {
                        handleLocalAvailabilityStatus(availableNowMap)
                        val request = AvailabilityMonitorRequest(listOf(container.containerUid),
                                onEntityAvailabilityChanged = this@ContentEntryDetailPresenter::handleLocalAvailabilityStatus)
                        availabilityMonitorRequest = request
                        localAvailabilityManager?.addMonitoringRequest(request)
                    })
                }

            }
        }
    }


    /**
     * Handle click download/open button
     */
    @JsName("handleDownloadButtonClick")
    fun handleDownloadButtonClick() {
        val canOpen = !isDownloadEnabled || downloadJobItemLiveData?.getValue()?.djiStatus == JobStatus.COMPLETE
        if (canOpen) {
            val loginFirst = impl.getAppConfigString(AppConfig.KEY_LOGIN_REQUIRED_FOR_CONTENT_OPEN,
                    "false", context)!!.toBoolean()

            if (loginFirst) {
                impl.go(LoginView.VIEW_NAME, args, view.viewContext)
            } else {
                goToContentEntry()
            }
        } else if (isDownloadEnabled) {
            view.runOnUiThread(Runnable {
                view.showDownloadOptionsDialog(mapOf("contentEntryUid" to this.entryUuid.toString()))
            })
        }
    }

    private fun goToContentEntry() {
        GlobalScope.launch {
            try {
                view.showBaseProgressBar(true)
                goToEntryFn(entryUuid, appDb, context, impl, isDownloadEnabled,
                        false,
                        arguments[ContentEntryListFragmentPresenter.ARG_NO_IFRAMES]
                                ?.toBoolean() ?: false)
            } catch (e: Exception) {
                if (e is NoAppFoundException) {
                    view.showFileOpenError(impl.getString(MessageID.no_app_found, context),
                            MessageID.get_app,
                            e.mimeType!!)
                } else {
                    view.showFileOpenError(e.message!!)
                }
            } finally {
                view.showBaseProgressBar(false)
            }
        }
    }


    @JsName("handleClickTranslatedEntry")
    fun handleClickTranslatedEntry(uid: Long) {
        val args = HashMap<String, String>()
        args[ARG_CONTENT_ENTRY_UID] = uid.toString()
        impl.go(ContentEntryDetailView.VIEW_NAME, args, context)
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


    fun handleLocalAvailabilityStatus(entryStatuses: Map<Long, Boolean>) {
        val localContainerUid = containerUid
        val isAvailable = localContainerUid != null && (entryStatuses[localContainerUid] ?: false)
        val icon = if (isAvailable)
            LOCALLY_AVAILABLE_ICON
        else
            LOCALLY_NOT_AVAILABLE_ICON

        val status = impl.getString(
                if (isAvailable)
                    MessageID.download_locally_availability
                else
                    MessageID.download_cloud_availability, context)

        view.runOnUiThread(Runnable { view.updateLocalAvailabilityViews(icon, status) })
    }


    fun handleShowEditControls(show: Boolean) {
        this.showEditorControls = show
    }

    @JsName("handleCancelDownload")
    suspend fun handleCancelDownload() {
        val currentJobId = appRepo.downloadJobDao.getLatestDownloadJobUidForContentEntryUid(entryUuid)
        appRepo.downloadJobDao.updateJobAndItems(currentJobId, JobStatus.CANCELED,
                JobStatus.CANCELLING)
        appRepo.contentEntryStatusDao.updateDownloadStatusAsync(entryUuid, JobStatus.CANCELED)
    }


    @JsName("handleStartEditingContent")
    fun handleStartEditingContent() {

        GlobalScope.launch {
            val entry = appRepo.contentEntryDao.findByEntryId(entryUuid)
            if (entry != null) {
                args.putAll(arguments)

                val imported = (entry.contentFlags and FLAG_IMPORTED) == FLAG_IMPORTED
                args[ContentEditorView.CONTENT_ENTRY_UID] = entryUuid.toString()
                args[ContentEntryEditView.CONTENT_ENTRY_LEAF] = true.toString()
                args[ContentEditorView.CONTENT_STORAGE_OPTION] = ""
                args[ContentEntryEditView.CONTENT_TYPE] = (if (imported) ContentEntryListView.CONTENT_IMPORT_FILE
                else ContentEntryListView.CONTENT_CREATE_CONTENT).toString()

                if (imported)
                    view.startFileBrowser(args)
                else
                    impl.go(ContentEditorView.VIEW_NAME, args, context)
            }
        }
    }

    @JsName("handleContentEntryExport")
    fun handleContentEntryExport() {
        val args = HashMap(arguments)
        args[ContentEntryExportView.ARG_CONTENT_ENTRY_TITLE] = currentContentEntry.title
        impl.go(ContentEntryExportView.VIEW_NAME, args, context)
    }


    override fun onDestroy() {
        val monitoringRequest = availabilityMonitorRequest
        if (monitoringRequest != null) {
            localAvailabilityManager?.removeMonitoringRequest(monitoringRequest)
            availabilityMonitorRequest = null
        }

        super.onDestroy()
    }

    companion object {

        const val ARG_CONTENT_ENTRY_UID = "entryid"

        const val ARG_CONTAINER_UID = "containerUid"

        const val LOCALLY_AVAILABLE_ICON = 1

        const val LOCALLY_NOT_AVAILABLE_ICON = 2

        private const val BAD_NODE_FAILURE_THRESHOLD = 3

        @JsName("getLicenseType")
        fun getLicenseType(licenseType: Int): String {
            when (licenseType) {
                ContentEntry.LICENSE_TYPE_CC_BY -> return "CC BY"
                ContentEntry.LICENSE_TYPE_CC_BY_SA -> return "CC BY SA"
                ContentEntry.LICENSE_TYPE_CC_BY_SA_NC -> return "CC BY SA NC"
                ContentEntry.LICENSE_TYPE_CC_BY_NC -> return "CC BY NC"
                ContentEntry.LICENSE_TYPE_CC_BY_NC_SA -> return "CC BY NC SA"
                ContentEntry.LICENSE_TYPE_PUBLIC_DOMAIN -> return "Public Domain"
                ContentEntry.ALL_RIGHTS_RESERVED -> return "All Rights Reserved"
            }
            return ""
        }
    }

}
