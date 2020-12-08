package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.InviteViaLinkView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import org.kodein.di.DI


class ClazzDetailOverviewPresenter(context: Any,
                          arguments: Map<String, String>, view: ClazzDetailOverviewView, di: DI,
                          lifecycleOwner: DoorLifecycleOwner)

    : UstadDetailPresenter<ClazzDetailOverviewView, ClazzWithDisplayDetails>(context, arguments, view,
        di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.LIVEDATA

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return db.clazzDao.personHasPermissionWithClazz(account?.personUid ?: 0L,
                arguments[ARG_ENTITY_UID]?.toLong() ?: 0L, Role.PERMISSION_CLAZZ_UPDATE)
    }

    override fun onLoadLiveData(repo: UmAppDatabase): DoorLiveData<ClazzWithDisplayDetails?>? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        view.scheduleList = repo.scheduleDao.findAllSchedulesByClazzUid(entityUid)
        GlobalScope.launch {
            view.clazzCodeVisible = repo.clazzDao.personHasPermissionWithClazz(
                    accountManager.activeAccount.personUid, entityUid,
                    Role.PERMISSION_CLAZZ_ADD_STUDENT)
        }

        return repo.clazzDao.getClazzWithDisplayDetails(entityUid)
    }

    override fun handleClickEdit() {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        systemImpl.go(ClazzEdit2View.VIEW_NAME, mapOf(ARG_ENTITY_UID to entityUid.toString()),
            context)
    }



}