package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.door.doorMainDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class SettingsPresenter(context: Any, arguments: Map<String, String>, view: SettingsView,
                        di: DI) :
        UstadBaseController<SettingsView>(context, arguments, view, di) {

    private val impl: UstadMobileSystemImpl by instance()

    val accountManager: UstadAccountManager by instance()

    val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_REPO)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        GlobalScope.launch(doorMainDispatcher()){
            val isAdmin = repo.personDao.personIsAdmin(accountManager.activeAccount.personUid)
            view.workspaceSettingsVisible = isAdmin
            // TODO check permission
            view.holidayCalendarVisible = true
            view.reasonLeavingVisible = true
            view.langListVisible = true
        }

    }

    fun goToHolidayCalendarList() {
        impl.go(HolidayCalendarListView.VIEW_NAME, mapOf(), context)
    }

    fun goToPeopleList() {
        impl.go(PersonListView.VIEW_NAME, mapOf(), context)
    }

    fun handleClickWorkspace() {
        impl.go(SiteDetailView.VIEW_NAME, mapOf(), context)
    }

    fun handleClickLeavingReason(){
        impl.go(LeavingReasonListView.VIEW_NAME, mapOf(), context)
    }

    fun handleClickLangList(){
        impl.go(LanguageListView.VIEW_NAME, mapOf(), context)
    }

}
