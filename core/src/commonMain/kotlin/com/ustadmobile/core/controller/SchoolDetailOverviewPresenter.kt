package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.SchoolDetailOverviewView
import com.ustadmobile.core.view.SchoolEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI


class SchoolDetailOverviewPresenter(context: Any, arguments: Map<String, String>,
                view: SchoolDetailOverviewView, di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadDetailPresenter<SchoolDetailOverviewView, SchoolWithHolidayCalendar>(context, arguments,
        view, di, lifecycleOwner) {

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
        systemImpl.go(SchoolEditView.VIEW_NAME, arguments, context)
    }

    fun handleClickClazz(clazz: Clazz) {
        systemImpl.go(ClazzDetailView.VIEW_NAME, mapOf(ARG_ENTITY_UID to clazz.clazzUid.toString()),
            context)
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        //TODO: this
        return true
    }

}