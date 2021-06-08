package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_CLAZZ_CONTENT_FILTER
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_CONTENT_FILTER
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_CLAZZUID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class ClazzDetailPresenter(context: Any,
                           arguments: Map<String, String>, view: ClazzDetailView, di: DI,
                           lifecycleOwner: DoorLifecycleOwner)
    : UstadDetailPresenter<ClazzDetailView, Clazz>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return false //This has no effect because the button is controlled by the overview presenter
    }

    override fun onLoadFromJson(bundle: Map<String, String>): Clazz? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: Clazz? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di,  Clazz.serializer(), entityJsonStr)
        }else {
            editEntity = Clazz()
        }

        GlobalScope.launch {
            setupTabs(editEntity)
        }

        return editEntity
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Clazz? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val clazz = withContext(Dispatchers.Default) {
            withTimeoutOrNull(2000) { db.clazzDao.findByUidAsync(entityUid) }
        } ?: Clazz()

        setupTabs(clazz)

        return clazz
    }

    private suspend fun setupTabs(clazz: Clazz) {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val personUid = accountManager.activeAccount.personUid
        GlobalScope.launch(doorMainDispatcher()) {
            view.tabs = listOf("${ClazzDetailOverviewView.VIEW_NAME}?$ARG_ENTITY_UID=$entityUid",
                    """${ContentEntryList2View.VIEW_NAME}?$ARG_FILTER_BY_CLAZZUID=$entityUid&
                       $ARG_CONTENT_FILTER=$ARG_CLAZZ_CONTENT_FILTER""".trimMargin(),
                    """${ClazzMemberListView.VIEW_NAME}?
                        $ARG_FILTER_BY_CLAZZUID=$entityUid
                        """.trimMargin()) +
                    CLAZZ_FEATURES.filter { featureFlag ->
                        (clazz.clazzFeatures and featureFlag) > 0 && db.clazzDao.personHasPermissionWithClazz(personUid, entityUid,
                                FEATURE_PERMISSION_MAP[featureFlag] ?: -1)
                    }.map {
                        (VIEWNAME_MAP[it] ?: "INVALID") + "?$ARG_FILTER_BY_CLAZZUID=$entityUid"
                    }
        }
    }

    override fun onLoadDataComplete() {
        super.onLoadDataComplete()

        observeSavedStateResult(ContentEntryList2Presenter.SAVEDSTATE_KEY_ENTRY,
                ListSerializer(ContentEntry.serializer()),
                ContentEntry::class) {
            val entry = it.firstOrNull() ?: return@observeSavedStateResult
            GlobalScope.launch {
                ClazzContentJoin().apply {
                    ccjClazzUid = arguments[ARG_ENTITY_UID]?.toLong() ?: return@apply
                    ccjContentEntryUid = entry.contentEntryUid
                    ccjUid = repo.clazzContentJoinDao.insert(this)
                }
            }
            requireSavedStateHandle()[ContentEntryList2Presenter.SAVEDSTATE_KEY_ENTRY] = null
        }

    }

    companion object {

        val CLAZZ_FEATURES = listOf(Clazz.CLAZZ_FEATURE_ATTENDANCE, Clazz.CLAZZ_FEATURE_CLAZZWORK)

        //Map of the feature flag to the permission flag required for that tab to be visible
        val FEATURE_PERMISSION_MAP = mapOf(
                Clazz.CLAZZ_FEATURE_ATTENDANCE to Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT,
                Clazz.CLAZZ_FEATURE_CLAZZWORK to Role.PERMISSION_CLAZZWORK_SELECT)

        val VIEWNAME_MAP = mapOf<Long, String>(
                Clazz.CLAZZ_FEATURE_ATTENDANCE to ClazzLogListAttendanceView.VIEW_NAME,
                Clazz.CLAZZ_FEATURE_CLAZZWORK to ClazzWorkListView.VIEW_NAME
        )
    }

}