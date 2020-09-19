package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
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
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        GlobalScope.launch {
            view.schoolCodeVisible = repo.schoolDao.personHasPermissionWithSchool(
                    accountManager.activeAccount.personUid, entityUid,
                    Role.PERMISSION_SCHOOL_UPDATE)
        }
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

    fun handleGoToInviteViaLink() {
        GlobalScope.launch {
            val school = repo.schoolDao.findByUidAsync(arguments[ARG_ENTITY_UID]?.toLong() ?: 0L)
            view.runOnUiThread(Runnable {
                systemImpl.go(InviteViaLinkView.VIEW_NAME, mapOf(
                        UstadView.ARG_CODE_TABLE to School.TABLE_ID.toString(),
                        UstadView.ARG_CODE to school?.schoolCode.toString(),
                        UstadView.ARG_ENTITY_NAME to school?.schoolCode.toString()
                ), context)
            })
        }
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return db.schoolDao.personHasPermissionWithSchool(account?.personUid ?: 0L,
                arguments[ARG_ENTITY_UID]?.toLong() ?: 0L, Role.PERMISSION_SCHOOL_UPDATE)
    }

}