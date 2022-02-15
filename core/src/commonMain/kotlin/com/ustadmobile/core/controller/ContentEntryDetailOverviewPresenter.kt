package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.storeCompletedStatement
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_DOWNLOAD_ENABLED
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.RateLimitedLiveData
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.util.ext.toDeepLink
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEARNER_GROUP_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_NO_IFRAMES
import com.ustadmobile.door.*
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.*
import org.kodein.di.*


class ContentEntryDetailOverviewPresenter(
    context: Any,
    arguments: Map<String, String>, view: ContentEntryDetailOverviewView,
    di: DI,
    lifecycleOwner: DoorLifecycleOwner
) : UstadDetailPresenter<ContentEntryDetailOverviewView, ContentEntryWithMostRecentContainer>(
    context, arguments, view, di, lifecycleOwner
){

    val deepLink: String
        get() {
            val activeEndpoint = di.direct.instance<UstadAccountManager>().activeAccount.endpointUrl
            return arguments.toDeepLink(activeEndpoint, ContentEntryDetailView.VIEW_NAME)
        }

    private val isDownloadEnabled: Boolean by di.instance(tag = TAG_DOWNLOAD_ENABLED)

    private val contentEntryOpener: ContentEntryOpener by di.on(accountManager.activeAccount).instance()

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    val statementEndpoint by on(accountManager.activeAccount).instance<XapiStatementEndpoint>()

    private var contentEntryUid = 0L

    private var clazzUid = 0L

    private var contextRegistration: String? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        contentEntryUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        clazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0L
    }


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ContentEntryWithMostRecentContainer? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val entity = withTimeoutOrNull(2000) {
            db.contentEntryDao.findEntryWithContainerByEntryId(entityUid)
        } ?: ContentEntryWithMostRecentContainer()

        val result = db.contentEntryRelatedEntryJoinDao.findAllTranslationsWithContentEntryUid(entityUid)
        view.availableTranslationsList = result

        view.scoreProgress = db.statementDao.getBestScoreForContentForPerson(entityUid, accountManager.activeAccount.personUid)
        if(entity.completionCriteria == ContentEntry.COMPLETION_CRITERIA_MARKED_BY_STUDENT){
            contextRegistration = db.statementDao.findLatestRegistrationStatement(
                    accountManager.activeAccount.personUid, entityUid)
            view.markCompleteVisible = contextRegistration != null
        }else{
            view.markCompleteVisible = false
        }

        if (db !is DoorDatabaseRepository) {
            db.containerDao.hasContainerWithFilesToDelete(entityUid)
                    .observeWithLifecycleOwner(lifecycleOwner) {
                        view.canDelete = it ?: false
                    }

            db.containerDao.hasContainerWithFilesToOpen(entityUid)
                    .observeWithLifecycleOwner(lifecycleOwner){
                        view.canOpen = it ?: false
                    }

            db.containerDao.hasContainerWithFilesToDownload(entityUid)
                    .observeWithLifecycleOwner(lifecycleOwner){
                        view.canDownload = it ?: false
                    }

            db.containerDao.hasContainerWithFilesToUpdate(entityUid)
                    .observeWithLifecycleOwner(lifecycleOwner){
                        view.canUpdate = it ?: false
                    }

            val contentJobItemStatusLiveData = RateLimitedLiveData(db, listOf("ContentJobItem"), 1000) {
                db.contentJobItemDao.findStatusForActiveContentJobItem(contentEntryUid)
            }

            val contentJobItemProgressLiveData = RateLimitedLiveData(db, listOf("ContentJobItem") , 1000){
                db.contentJobItemDao.findProgressForActiveContentJobItem(contentEntryUid)
            }

            withContext(doorMainDispatcher()){
                contentJobItemStatusLiveData.observeWithLifecycleOwner(lifecycleOwner){
                    val status = it ?: return@observeWithLifecycleOwner
                    view.contentJobItemStatus = status
                }
                contentJobItemProgressLiveData.observeWithLifecycleOwner(lifecycleOwner){
                    val progress = it ?: return@observeWithLifecycleOwner
                    view.contentJobItemProgress = progress
                }

            }

        }

        return entity
    }

    override fun handleClickEdit() {
        systemImpl.go(ContentEntryEdit2View.VIEW_NAME,
                mapOf(ARG_ENTITY_UID to entity?.contentEntryUid.toString(),
                        ARG_LEAF to true.toString()), context)
    }

    fun handleOnClickOpenDownloadButton() {
        presenterScope.launch {
            val containerWithFiles = db.containerDao.findContainerWithFilesByContentEntryUid(contentEntryUid)
            val canOpen = !isDownloadEnabled || (containerWithFiles != null && containerWithFiles.containerUid != 0L)
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
    }

    fun handleOnClickManageDownload() {
        view.showDownloadDialog(mapOf(ARG_CONTENT_ENTRY_UID to (entity?.contentEntryUid?.toString()
            ?: "0")))
    }

    private fun openContentEntry() {
        presenterScope.launch(doorMainDispatcher()) {
            try {
                entity?.contentEntryUid?.also {
                    contentEntryOpener.openEntry(context, it, isDownloadEnabled, false,
                            arguments[ARG_NO_IFRAMES]?.toBoolean() ?: false, clazzUid = clazzUid)
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
        systemImpl.go(ContentEntryDetailView.VIEW_NAME, mapOf(ARG_ENTITY_UID to entryUid.toString(),
                                            ARG_CLAZZUID to clazzUid.toString()), context)
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return db.contentEntryDao.personHasPermissionWithContentEntry(accountManager.activeAccount.personUid,
                arguments[ARG_ENTITY_UID]?.toLong() ?: 0, Role.PERMISSION_CONTENT_UPDATE)
    }

    fun handleOnClickDeleteButton() {
        handleOnClickManageDownload()
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
                            ARG_LEARNER_GROUP_UID to learnerGroup.learnerGroupUid.toString(),
                            ARG_CLAZZUID to clazzUid.toString()),
                    context)
        }
    }

    fun handleOnClickMarkComplete() {
        if(accountManager.activeAccount.personUid == 0L)
            return //no one is really logged in

        GlobalScope.launch {
            val contentEntry = view.entity ?: return@launch
            statementEndpoint.storeCompletedStatement(accountManager.activeAccount, contentEntry,
                    contextRegistration ?: "", null, clazzUid)
        }
        view.markCompleteVisible = false
        val scoreProgress = view.scoreProgress
        scoreProgress?.contentComplete = true
        scoreProgress?.progress = 100
        view.scoreProgress = scoreProgress
    }

}