package com.ustadmobile.core.controller

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.UmObserver
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.NetworkNodeDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.ARG_REFERRER
import com.ustadmobile.core.networkmanager.DownloadJobItemStatusProvider
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor
import com.ustadmobile.core.networkmanager.OnDownloadJobItemChangeListener
import com.ustadmobile.core.util.ContentEntryUtil
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.ContentEntryListFragmentView
import com.ustadmobile.core.view.DummyView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryStatus
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class ContentEntryDetailPresenter(context: Any, arguments: Map<String, String?>,
                                  viewContract: ContentEntryDetailView,
                                  private val monitor: LocalAvailabilityMonitor,
                                  private val statusProvider: DownloadJobItemStatusProvider)
    : UstadBaseController<ContentEntryDetailView>(context, arguments, viewContract),
        OnDownloadJobItemChangeListener {

    private var navigation: String? = null

    var entryUuid: Long = 0
        private set

    private var containerUid: Long? = 0L

    private var networkNodeDao: NetworkNodeDao? = null

    private var containerDao: ContainerDao? = null

    private val monitorStatus = atomic(false)

    private val isListeningToDownloadStatus = atomic(false)

    private var statusUmLiveData: DoorLiveData<ContentEntryStatus>? = null

    private val statusUmObserver: UmObserver<ContentEntryStatus> =
            object : UmObserver<ContentEntryStatus> {
                override fun onChanged(t: ContentEntryStatus?) {
                    when (t) {
                        null -> onEntryStatusChanged(null)
                        else -> onEntryStatusChanged(t)
                    }
                }
            }

    private val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        val repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context)
        val appdb = UmAppDatabase.getInstance(context)
        val contentRelatedEntryDao = repoAppDatabase.contentEntryRelatedEntryJoinDao
        val contentEntryDao = repoAppDatabase.contentEntryDao
        val contentEntryStatusDao = appdb.contentEntryStatusDao
        containerDao = repoAppDatabase.containerDao
        networkNodeDao = appdb.networkNodeDao

        entryUuid = arguments.getValue(ARG_CONTENT_ENTRY_UID)!!.toLong()
        navigation = arguments.getValue(ARG_REFERRER) ?: ""

        GlobalScope.launch {
            val result = contentEntryDao.getContentByUuidAsync(entryUuid)
            if (result != null) {
                val licenseType = getLicenseType(result)
                view.runOnUiThread(Runnable {
                    view.setContentEntryLicense(licenseType)
                    with(result) {
                        val contentEntryAuthor = author
                        if (contentEntryAuthor != null)
                            view.setContentEntryAuthor(contentEntryAuthor)

                        val contentEntryTitle = title
                        if (contentEntryTitle != null)
                            view.setContentEntryTitle(contentEntryTitle)

                        val contentEntryDesc = description
                        if (contentEntryDesc != null)
                            view.setContentEntryDesc(contentEntryDesc)

                        val contentThumbnailUrl = thumbnailUrl
                        if (!contentThumbnailUrl.isNullOrEmpty())
                            view.loadEntryDetailsThumbnail(contentThumbnailUrl)
                    }
                })
            }
        }

        GlobalScope.launch {
            val result = containerDao!!.findFilesByContentEntryUid(entryUuid)
            view.runOnUiThread(Runnable {
                view.setDetailsButtonEnabled(result.isNotEmpty())
                if (result.isNotEmpty()) {
                    val container = result[0]
                    view.setDownloadSize(container.fileSize)
                }
            })
        }

        GlobalScope.launch {
            val result = contentRelatedEntryDao.findAllTranslationsForContentEntryAsync(entryUuid)
            view.runOnUiThread(Runnable {
                view.setTranslationLabelVisible(result.isNotEmpty())
                view.setFlexBoxVisible(result.isNotEmpty())
                view.setAvailableTranslations(result, entryUuid)
            })
        }

        statusUmLiveData = contentEntryStatusDao.findContentEntryStatusByUid(entryUuid)

        statusUmLiveData!!.observe(this, statusUmObserver)
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

        val isDownloadComplete = status != null && status.downloadStatus == JobStatus.COMPLETE

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
            statusProvider.addDownloadChangeListener(this)
            statusProvider.findDownloadJobItemStatusByContentEntryUid(entryUuid,
                    object : UmResultCallback<DownloadJobItemStatus?> {
                        override fun onDone(result: DownloadJobItemStatus?) {
                            onDownloadJobItemChange(result, result?.jobItemUid ?: 0)
                        }
                    })
            view.setDownloadButtonVisible(false)
            view.setDownloadProgressVisible(true)
        } else if (!isDownloading && isListeningToDownloadStatus.value) {
            isListeningToDownloadStatus.value = false
            statusProvider.removeDownloadChangeListener(this)
            view.setDownloadButtonVisible(true)
            view.setDownloadProgressVisible(false)
        }

        view.runOnUiThread(Runnable {
            view.setButtonTextLabel(buttonLabel)
            view.setDownloadButtonVisible(!isDownloading)
            view.setDownloadButtonClickableListener(isDownloadComplete)
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

                val container = containerDao!!.getMostRecentContainerForContentEntry(entryUuid)
                if (container != null) {
                    containerUid = container.containerUid
                    val localNetworkNode = networkNodeDao!!.findLocalActiveNodeByContainerUid(
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

    fun handleClickTranslatedEntry(uid: Long) {
        val args = HashMap<String, String>()
        args[ARG_CONTENT_ENTRY_UID] = uid.toString()
        impl.go(ContentEntryDetailView.VIEW_NAME, args, view.viewContext!!)
    }

    fun handleUpNavigation() {
        val lastEntryListArgs = UMFileUtil.getLastReferrerArgsByViewname(ContentEntryListFragmentView.VIEW_NAME, navigation!!)
        if (lastEntryListArgs != null) {
            impl.go(ContentEntryListFragmentView.VIEW_NAME,
                    UMFileUtil.parseURLQueryString(lastEntryListArgs), context,
                    UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP)
        } else {
            impl.go(DummyView.VIEW_NAME, mutableMapOf(), context,
                    UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP)
        }
    }

    fun handleDownloadButtonClick(isDownloadComplete: Boolean, entryUuid: Long) {
        val repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context)
        if (isDownloadComplete) {
            ContentEntryUtil.goToContentEntry(entryUuid, repoAppDatabase, impl, isDownloadComplete,
                    context, object : UmCallback<Any> {
                override fun onSuccess(result: Any?) {

                }

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


    override fun onDestroy() {
        if (monitorStatus.value) {
            monitorStatus.value = false
            monitor.stopMonitoringAvailability(this)
        }

        statusUmLiveData?.removeObserver(statusUmObserver)


        if (isListeningToDownloadStatus.getAndSet(false)) {
            statusProvider.removeDownloadChangeListener(this)
        }
        super.onDestroy()
    }

    companion object {

        const val ARG_CONTENT_ENTRY_UID = "entryid"

        const val LOCALLY_AVAILABLE_ICON = 1

        const val LOCALLY_NOT_AVAILABLE_ICON = 2

        private const val BAD_NODE_FAILURE_THRESHOLD = 3

        private const val TIME_INTERVAL_FROM_LAST_FAILURE = 5

        const val NO_ACTIVITY_FOR_FILE_FOUND = "No activity found for mimetype"
    }

}
