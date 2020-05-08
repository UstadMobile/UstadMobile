package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Clazz

import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_CLAZZUID
import com.ustadmobile.lib.db.entities.Role


typealias ClazzPermissionChecker = suspend (db: UmAppDatabase, personUid: Long, clazzUid: Long) -> Boolean

class ClazzDetailPresenter(context: Any,
                           arguments: Map<String, String>, view: ClazzDetailView,
                           lifecycleOwner: DoorLifecycleOwner,
                           systemImpl: UstadMobileSystemImpl,
                           db: UmAppDatabase, repo: UmAppDatabase,
                           activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadDetailPresenter<ClazzDetailView, Clazz>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

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
        return true
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Clazz? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val clazz = withTimeoutOrNull(2000) {
             db.clazzDao.findByUid(entityUid)
        } ?: Clazz()

        val activePersonUid = activeAccount.getValue()?.personUid ?: 0L

        view.tabs = listOf("${ClazzDetailOverviewView.VIEW_NAME}?$ARG_ENTITY_UID=$entityUid",
                "${ClazzMemberListView.VIEW_NAME}?$ARG_FILTER_BY_CLAZZUID=$entityUid") +
                CLAZZ_FEATURES.filter {
                PERMISSION_CHECKER_MAP[it]?.invoke(db, activePersonUid, entityUid) ?: false
        }.map { (VIEWNAME_MAP[it] ?: "INVALID}") + "?$ARG_FILTER_BY_CLAZZUID=$entityUid"}

        return clazz
    }

    companion object {

        val CLAZZ_FEATURES = listOf(Clazz.CLAZZ_FEATURE_ATTENDANCE)

        val PERMISSION_CHECKER_MAP = mapOf<Long, ClazzPermissionChecker>(
                Clazz.CLAZZ_FEATURE_ATTENDANCE to {db, personUid, clazzUid ->
                    db.clazzDao.personHasPermissionWithClazz(personUid, clazzUid,
                    Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT)
                }
        )

        val VIEWNAME_MAP = mapOf<Long, String>(
                Clazz.CLAZZ_FEATURE_ATTENDANCE to ""
        )
    }

}