package com.ustadmobile.core.controller

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.GoToEntryFn
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.util.goToContentEntry
import com.ustadmobile.core.view.ContentEntry2DetailView
import com.ustadmobile.core.view.ContentEntryAddOptionsView.Companion.CONTENT_CREATE_CONTENT
import com.ustadmobile.core.view.ContentEntryAddOptionsView.Companion.CONTENT_CREATE_FOLDER
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.LoginView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull


class ContentEntry2DetailPresenter(context: Any,
                                   arguments: Map<String, String>, view: ContentEntry2DetailView,
                                   lifecycleOwner: DoorLifecycleOwner,
                                   systemImpl: UstadMobileSystemImpl,
                                   private val isDownloadEnabled: Boolean,
                                   db: UmAppDatabase, repo: UmAppDatabase,
                                   private val containerDownloadManager: ContainerDownloadManager?,
                                   activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData,
                                   private val goToEntryFn: GoToEntryFn = ::goToContentEntry)
    : UstadDetailPresenter<ContentEntry2DetailView, ContentEntryWithMostRecentContainer>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private var downloadJobItemLiveData: DoorLiveData<DownloadJobItem?>? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val entryUuid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        if (containerDownloadManager != null) {
            GlobalScope.launch(doorMainDispatcher()) {
                downloadJobItemLiveData = containerDownloadManager.getDownloadJobItemByContentEntryUid(entryUuid).apply {
                    observeWithPresenter(this@ContentEntry2DetailPresenter, this@ContentEntry2DetailPresenter::onDownloadJobItemChanged)
                }
            }
        }
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ContentEntryWithMostRecentContainer? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val entity = withTimeoutOrNull(2000) {
            db.contentEntryDao.findEntryWithContainerByEntryId(entityUid)
        } ?: ContentEntryWithMostRecentContainer()

        val result = db.contentEntryRelatedEntryJoinDao.findAllTranslationsWithContentEntryUid(entityUid)
        view.setAvailableTranslations(result)
        return entity
    }

    override fun handleClickEdit() {
        val args = HashMap(arguments)
        args[ContentEntryEdit2View.CONTENT_TYPE] = CONTENT_CREATE_CONTENT.toString()
        systemImpl.go(ContentEntryEdit2View.VIEW_NAME, args, context)
    }

    fun handleOnClickOpenDownloadButton(){
        val canOpen = !isDownloadEnabled || downloadJobItemLiveData?.getValue()?.djiStatus == JobStatus.COMPLETE
        if (canOpen) {
            val loginFirst = systemImpl.getAppConfigString(AppConfig.KEY_LOGIN_REQUIRED_FOR_CONTENT_OPEN,
                    "false", context)!!.toBoolean()

            if (loginFirst) {
                systemImpl.go(LoginView.VIEW_NAME, arguments, context)
            } else {
                goToSelectedContentEntry()
            }
        } else if (isDownloadEnabled) {
            //show dialog options
        }
    }

    private fun onDownloadJobItemChanged(downloadJobItem: DownloadJobItem?) {

    }

    private fun goToSelectedContentEntry() {
        GlobalScope.launch {
            try {
                entity?.contentEntryUid?.let { goToEntryFn(it, db, context, systemImpl, isDownloadEnabled,
                            false,
                            arguments[ContentEntryListPresenter.ARG_NO_IFRAMES]
                                    ?.toBoolean() ?: false) }
            } catch (e: Exception) {
                if (e is NoAppFoundException) {
                    //show no app error snack
                } else {
                    //Failed to open
                }
            }
        }
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return true
    }

}