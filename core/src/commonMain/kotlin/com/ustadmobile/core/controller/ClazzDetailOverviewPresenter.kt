package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchool
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
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
        presenterScope.launch {
            view.clazzCodeVisible = repo.clazzDao.personHasPermissionWithClazz(
                    accountManager.activeAccount.personUid, entityUid,
                    Role.PERMISSION_CLAZZ_ADD_STUDENT)
            view.courseBlockList = repo.courseBlockDao.findAllCourseBlockByClazzUidLive(entityUid)
        }

        return repo.clazzDao.getClazzWithDisplayDetails(entityUid, systemTimeInMillis())
    }

    override fun handleClickEdit() {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        navigateForResult(
            NavigateForResultOptions(this,
                null, ClazzEdit2View.VIEW_NAME, ClazzWithHolidayCalendarAndSchool::class,
                ClazzWithHolidayCalendarAndSchool.serializer(), SAVEDSTATE_KEY_CLAZZ,
                arguments = mutableMapOf(ARG_ENTITY_UID to entityUid.toString())
            )
        )
    }

    companion object {
        const val SAVEDSTATE_KEY_CLAZZ = "Clazz"
    }



}