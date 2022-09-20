package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI


class SchoolDetailOverviewPresenter(context: Any, arguments: Map<String, String>,
                view: SchoolDetailOverviewView, di: DI, lifecycleOwner: LifecycleOwner)
    : UstadDetailPresenter<SchoolDetailOverviewView, SchoolWithHolidayCalendar>(context, arguments,
        view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    var loggedInPersonUid = 0L


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        loggedInPersonUid = accountManager.activeAccount.personUid

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
            repo.clazzDao.findClazzesWithPermission(
                    "".toQueryLikeParam(),
                    loggedInPersonUid, listOf(), 0, 0,
                    0, systemTimeInMillis(), Role.PERMISSION_CLAZZ_SELECT,  entityUid)
        }
        view.schoolClazzes = clazzes

        return schoolWithHolidayCalendar
    }

    override fun handleClickEdit() {
       navigateForResult(
            NavigateForResultOptions(this,
                null, SchoolEditView.VIEW_NAME, SchoolWithHolidayCalendar::class,
                SchoolWithHolidayCalendar.serializer(), SAVEDSTATE_KEY_SCHOOL,
                arguments = arguments.toMutableMap())
        )
    }

    fun handleClickClazz(clazz: Clazz) {
        systemImpl.go(ClazzDetailView.VIEW_NAME, mapOf(ARG_ENTITY_UID to clazz.clazzUid.toString()),
                context)
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        Napier.d(account?.personUid.toString())
        return db.schoolDao.personHasPermissionWithSchool(account?.personUid ?: 0L,
                arguments[ARG_ENTITY_UID]?.toLong() ?: 0L, Role.PERMISSION_SCHOOL_UPDATE)
    }

    companion object {
        const val SAVEDSTATE_KEY_SCHOOL = "School"
    }

}