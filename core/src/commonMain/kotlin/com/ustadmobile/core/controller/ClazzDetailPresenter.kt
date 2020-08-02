package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_CLAZZUID
import com.ustadmobile.lib.db.entities.Role
import kotlinx.serialization.json.Json
import org.kodein.di.DI


typealias ClazzPermissionChecker = suspend (db: UmAppDatabase, personUid: Long, clazzUid: Long) -> Boolean

class ClazzDetailPresenter(context: Any,
                           arguments: Map<String, String>, view: ClazzDetailView, di: DI,
                           lifecycleOwner: DoorLifecycleOwner)
    : UstadDetailPresenter<ClazzDetailView, Clazz>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //TODO: Set any additional fields (e.g. joinlist) on the view
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return false //This has no effect because the button is controlled by the overview presenter
    }

    override fun onLoadFromJson(bundle: Map<String, String>): Clazz? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: Clazz? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(Clazz.serializer(), entityJsonStr)
        }else {
            editEntity = Clazz()
        }

        GlobalScope.launch {
            setupTabs()
        }

        return editEntity
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Clazz? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val clazz = withTimeoutOrNull(2000) {
             db.clazzDao.findByUid(entityUid)
        } ?: Clazz()

        setupTabs()

        return clazz
    }

    private suspend fun setupTabs() {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val personUid = accountManager.activeAccount.personUid
        view.tabs = listOf("${ClazzDetailOverviewView.VIEW_NAME}?$ARG_ENTITY_UID=$entityUid",
                "${ClazzMemberListView.VIEW_NAME}?$ARG_FILTER_BY_CLAZZUID=$entityUid") +
                CLAZZ_FEATURES.filter {
                    db.clazzDao.personHasPermissionWithClazz(personUid, entityUid,
                        FEATURE_PERMISSION_MAP[it] ?: -1)
                }.map {
                    (VIEWNAME_MAP[it] ?: "INVALID}") + "?$ARG_FILTER_BY_CLAZZUID=$entityUid"
                }
    }

    companion object {

        val CLAZZ_FEATURES = listOf(Clazz.CLAZZ_FEATURE_ATTENDANCE)

        //Map of the feature flag to the permission flag required for that tab to be visible
        val FEATURE_PERMISSION_MAP = mapOf(
                Clazz.CLAZZ_FEATURE_ATTENDANCE to Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT)

        val VIEWNAME_MAP = mapOf<Long, String>(
                Clazz.CLAZZ_FEATURE_ATTENDANCE to ClazzLogListAttendanceView.VIEW_NAME
        )
    }

}