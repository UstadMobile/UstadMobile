package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SchoolDetailOverviewView
import com.ustadmobile.core.view.SchoolEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar

import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.School
import kotlinx.coroutines.withTimeoutOrNull


class SchoolDetailOverviewPresenter(context: Any,
                                    arguments: Map<String, String>, view: SchoolDetailOverviewView,
                                    lifecycleOwner: DoorLifecycleOwner,
                                    systemImpl: UstadMobileSystemImpl,
                                    db: UmAppDatabase, repo: UmAppDatabase,
                                    activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadDetailPresenter<SchoolDetailOverviewView, SchoolWithHolidayCalendar>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): SchoolWithHolidayCalendar? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

         val schoolWithHolidayCalendar = withTimeoutOrNull(2000) {
             db.schoolDao.findByUidWithHolidayCalendarAsync(entityUid)
         } ?: SchoolWithHolidayCalendar()

        val clazzes = withTimeoutOrNull(2000) {
            db.clazzDao.findAllClazzesBySchoolLive(entityUid)
        }
        view.schoolClazzes = clazzes

        return schoolWithHolidayCalendar

    }


    override fun handleClickEdit() {
        val impl = UstadMobileSystemImpl.instance
        impl.go(SchoolEditView.VIEW_NAME, arguments, context)
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        //TODO: this
        return true
    }

    companion object {


    }


}