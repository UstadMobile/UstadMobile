package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI


class ClazzDetailOverviewPresenter(context: Any,
                          arguments: Map<String, String>, view: ClazzDetailOverviewView, di: DI,
                          lifecycleOwner: DoorLifecycleOwner)

    : UstadDetailPresenter<ClazzDetailOverviewView, ClazzWithDisplayDetails>(context, arguments, view,
        di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.LIVEDATA

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

    override fun onLoadLiveData(repo: UmAppDatabase): DoorLiveData<ClazzWithDisplayDetails?>? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        view.scheduleList = repo.scheduleDao.findAllSchedulesByClazzUid(entityUid)
        return repo.clazzDao.getClazzWithDisplayDetails(entityUid)
    }

    override fun handleClickEdit() {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        systemImpl.go(ClazzEdit2View.VIEW_NAME, mapOf(ARG_ENTITY_UID to entityUid.toString()),
            context)
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}