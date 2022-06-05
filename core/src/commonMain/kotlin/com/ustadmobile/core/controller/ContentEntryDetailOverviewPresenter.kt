package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.storeCompletedStatement
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ContentPluginIds
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_DOWNLOAD_ENABLED
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.RateLimitedLiveData
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.util.ext.toDeepLink
import com.ustadmobile.core.view.ContentEntryDetailOverviewView
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.LearnerGroupMemberListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEARNER_GROUP_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_NO_IFRAMES
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


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

    //True on Android, false on the web
    private val isPlatformDownloadEnabled: Boolean by di.instance(tag = TAG_DOWNLOAD_ENABLED)

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


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ContentEntryWithMostRecentContainer {
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
            RateLimitedLiveData(db, listOf("Container", "ContentEntry", "ContentJobItem")) {
                db.contentEntryDao.buttonsToShowForContentEntry(entityUid, isPlatformDownloadEnabled)
            }.observeWithLifecycleOwner(lifecycleOwner) {
                if(it == null)
                    return@observeWithLifecycleOwner

                view.contentEntryButtons = it
            }


            RateLimitedLiveData(db, CONTENT_JOB_ITEM_TABLE_LIST,
                1000
            ) {
                db.contentJobItemDao.findActiveContentJobItems(contentEntryUid)
            }.observeWithLifecycleOwner(lifecycleOwner){
                val progress = it ?: return@observeWithLifecycleOwner
                view.activeContentJobItems = progress
            }
        }

        return entity
    }

    override fun handleClickEdit() {
        val args = mutableMapOf(
            ARG_ENTITY_UID to entity?.contentEntryUid.toString(),
            ARG_LEAF to true.toString())
        navigateForResult(
            NavigateForResultOptions(this,
                null, ContentEntryEdit2View.VIEW_NAME,
                ContentEntry::class,
                ContentEntry.serializer(),
                arguments = args)
        )
    }

    fun handleClickOpenButton() {
        openContentEntry()
    }

    fun handleClickDownloadButton() {
        view.showDownloadDialog(mapOf(ARG_CONTENT_ENTRY_UID to (entity?.contentEntryUid?.toString()
            ?: "0")))
    }

    fun handleOnClickManageDownload() {
        view.showDownloadDialog(mapOf(ARG_CONTENT_ENTRY_UID to (entity?.contentEntryUid?.toString()
            ?: "0")))
    }

    private fun openContentEntry() {
        presenterScope.launch(doorMainDispatcher()) {
            try {
                entity?.contentEntryUid?.also {
                    contentEntryOpener.openEntry(context, it, isPlatformDownloadEnabled, false,
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

    fun handleOnClickConfirmDelete() {
        presenterScope.launch {
            val job = db.withDoorTransactionAsync(UmAppDatabase::class) { txDb ->
                val job = ContentJob().apply {
                    cjNotificationTitle = systemImpl.getString(MessageID.deleting_content, context)
                        .replace("%1\$s",entity?.title ?: "")
                    cjUid = txDb.contentJobDao.insertAsync(this)
                }

                txDb.contentJobItemDao.insertJobItem(ContentJobItem().apply {
                    cjiJobUid = job.cjUid
                    cjiContentEntryUid = entity?.contentEntryUid ?: 0L
                    cjiPluginId = ContentPluginIds.DELETE_CONTENT_ENTRY_PLUGIN
                    cjiIsLeaf = true
                    cjiConnectivityNeeded = false
                    cjiStatus = JobStatus.QUEUED
                    sourceUri = entity?.toDeepLink(accountManager.activeEndpoint)
                })

                job
            }
            val contentJobManager : ContentJobManager = di.direct.instance()
            contentJobManager.enqueueContentJob(accountManager.activeEndpoint, job.cjUid)
        }
    }

    fun handleOnClickGroupActivityButton() {
        presenterScope.launch(doorMainDispatcher()) {
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

    companion object {
        private val CONTENT_JOB_ITEM_TABLE_LIST = listOf("ContentJobItem")
    }

}