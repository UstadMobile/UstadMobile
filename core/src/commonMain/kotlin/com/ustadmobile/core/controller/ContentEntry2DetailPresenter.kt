package com.ustadmobile.core.controller

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_DOWNLOAD_ENABLED
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest
import com.ustadmobile.core.networkmanager.DeletePreparationRequester
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.view.ContentEntry2DetailView
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.LearnerGroupMemberListView
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEARNER_GROUP_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_NO_IFRAMES
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.*
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.on


class ContentEntry2DetailPresenter(context: Any,
                                   arguments: Map<String, String>, view: ContentEntry2DetailView,
                                   di: DI, lifecycleOwner: DoorLifecycleOwner)

    : UstadDetailPresenter<ContentEntry2DetailView, ContentEntryWithMostRecentContainer>(context,
        arguments, view, di, lifecycleOwner) {

    private val isDownloadEnabled: Boolean by di.instance<Boolean>(tag = TAG_DOWNLOAD_ENABLED)

    private val containerDownloadManager: ContainerDownloadManager? by di.on(accountManager.activeAccount).instanceOrNull()

    private val contentEntryOpener: ContentEntryOpener by di.on(accountManager.activeAccount).instance()

    private val localAvailabilityManager: LocalAvailabilityManager? by on(accountManager.activeAccount).instanceOrNull()

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private var downloadJobItemLiveData: DoorLiveData<DownloadJobItem?>? = null

    private var availabilityRequest: AvailabilityMonitorRequest? = null

    private val availabilityRequestDeferred = CompletableDeferred<AvailabilityMonitorRequest>()

    private var contentEntryUid = 0L

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        contentEntryUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        println(containerDownloadManager)
        containerDownloadManager?.also {
            GlobalScope.launch(doorMainDispatcher()) {
                downloadJobItemLiveData = it.getDownloadJobItemByContentEntryUid(contentEntryUid).apply {
                    observeWithLifecycleOwner(lifecycleOwner, this@ContentEntry2DetailPresenter::onDownloadJobItemChanged)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        GlobalScope.launch {
            localAvailabilityManager?.addMonitoringRequest(availabilityRequestDeferred.await())
        }
    }

    override fun onStop() {
        GlobalScope.launch {
            localAvailabilityManager?.removeMonitoringRequest(availabilityRequestDeferred.await())
        }
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ContentEntryWithMostRecentContainer? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val entity = withTimeoutOrNull(2000) {
            db.contentEntryDao.findEntryWithContainerByEntryId(entityUid)
        } ?: ContentEntryWithMostRecentContainer()

        val result = db.contentEntryRelatedEntryJoinDao.findAllTranslationsWithContentEntryUid(entityUid)
        view.availableTranslationsList = result

        view.contentEntryProgress = db.contentEntryProgressDao.getProgressByContentAndPersonAsync(entityUid, accountManager.activeAccount.personUid)

        if (db == repo) {
            val containerUid = entity.container?.containerUid ?: 0L
            availabilityRequest = AvailabilityMonitorRequest(listOf(containerUid)) { availableEntries ->
                GlobalScope.launch(doorMainDispatcher()) {
                    view.locallyAvailable = availableEntries[containerUid] ?: false
                }
            }.also {
                availabilityRequestDeferred.complete(it)
            }
        }

        return entity
    }

    override fun handleClickEdit() {
        systemImpl.go(ContentEntryEdit2View.VIEW_NAME,
                mapOf(ARG_ENTITY_UID to entity?.contentEntryUid.toString()), context)
    }

    fun handleOnClickOpenDownloadButton() {
        val canOpen = !isDownloadEnabled || downloadJobItemLiveData?.getValue()?.djiStatus == JobStatus.COMPLETE
        if (canOpen) {
            val loginFirst = systemImpl.getAppConfigString(AppConfig.KEY_LOGIN_REQUIRED_FOR_CONTENT_OPEN,
                    "false", context)!!.toBoolean()
            val account = accountManager.activeAccount
            if (loginFirst && account.personUid == 0L) {
                systemImpl.go(Login2View.VIEW_NAME, arguments, context)
            } else {
                openContentEntry()
            }
        } else if (isDownloadEnabled) {
            view.showDownloadDialog(mapOf(ARG_CONTENT_ENTRY_UID to (entity?.contentEntryUid?.toString()
                    ?: "0")))
        }
    }

    fun handleOnClickManageDownload() {
        view.showDownloadDialog(mapOf(ARG_CONTENT_ENTRY_UID to (entity?.contentEntryUid?.toString()
            ?: "0")))
    }

    private fun onDownloadJobItemChanged(downloadJobItem: DownloadJobItem?) {
        view.downloadJobItem = downloadJobItem
    }

    private fun openContentEntry() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                entity?.contentEntryUid?.also {
                    contentEntryOpener.openEntry(context, it, isDownloadEnabled, false,
                            arguments[ARG_NO_IFRAMES]?.toBoolean() ?: false)
                }
            } catch (e: Exception) {
                if (e is NoAppFoundException) {
                    view.showSnackBar(systemImpl.getString(MessageID.no_app_found, context))
                } else {
                    val message = e.message
                    if (message != null) {
                        view.showSnackBar(message)
                    }
                }
            }
        }
    }

    fun handleOnTranslationClicked(entryUid: Long) {
        systemImpl.go(ContentEntry2DetailView.VIEW_NAME, mapOf(ARG_ENTITY_UID to entryUid.toString()), context)
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return db.contentEntryDao.personHasPermissionWithContentEntry(accountManager.activeAccount.personUid,
                arguments[ARG_ENTITY_UID]?.toLong() ?: 0, Role.PERMISSION_CONTENT_UPDATE)
    }

    fun handleOnClickDeleteButton() {
        GlobalScope.launch(doorMainDispatcher()){
            val downloadJobItem = db.downloadJobItemDao.findByContentEntryUidAsync(contentEntryUid) ?: return@launch
            val deleteRequester: DeletePreparationRequester by on(accountManager.activeAccount).instance()
            deleteRequester.requestDelete(downloadJobItem.djiUid)
        }
    }

    fun handleOnClickGroupActivityButton() {
        GlobalScope.launch(doorMainDispatcher()) {
            val learnerGroup = LearnerGroup().apply {
                learnerGroupUid = repo.learnerGroupDao.insertAsync(this)
            }
            GroupLearningSession().apply {
                groupLearningSessionContentUid = contentEntryUid
                groupLearningSessionLearnerGroupUid = learnerGroup.learnerGroupUid
                groupLearningSessionLearnerGroupUid = repo.groupLearningSessionDao.insertAsync(this)
            }
            LearnerGroupMember().apply {
                learnerGroupMemberRole = LearnerGroupMember.PRIMARY_ROLE
                learnerGroupMemberLgUid = learnerGroup.learnerGroupUid
                learnerGroupMemberPersonUid = accountManager.activeAccount.personUid
                learnerGroupMemberUid = repo.learnerGroupMemberDao.insertAsync(this)
            }
            systemImpl.go(LearnerGroupMemberListView.VIEW_NAME,
                    mapOf(ARG_CONTENT_ENTRY_UID to contentEntryUid.toString(),
                            ARG_LEARNER_GROUP_UID to learnerGroup.learnerGroupUid.toString()),
                    context)
        }
    }

}