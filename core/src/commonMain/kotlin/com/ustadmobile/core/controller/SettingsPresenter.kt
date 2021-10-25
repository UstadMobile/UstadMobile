package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
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

    private val languageOptions = impl.getAllUiLanguagesList(context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        GlobalScope.launch(doorMainDispatcher()){
            val selectedLocaleIndex = languageOptions.indexOfFirst { it.first == impl.getLocale(context) }
            view.setLanguageOptions(languageOptions.map { it.second }, languageOptions[selectedLocaleIndex].second)

            val isAdmin = repo.personDao.personIsAdmin(accountManager.activeAccount.personUid)
            view.workspaceSettingsVisible = isAdmin
            // TODO check permission
            view.holidayCalendarVisible = true
            view.reasonLeavingVisible = true
            view.langListVisible = true
        }

    }

    fun handleLanguageSelected(position: Int){
        val newLocaleCode = languageOptions[position].first
        val newLocaleToDisplay = if(newLocaleCode == UstadMobileSystemCommon.LOCALE_USE_SYSTEM) {
            impl.getSystemLocale(context).substring(0, 2)
        }else {
            newLocaleCode
        }

        val needsRestart = impl.getDisplayedLocale(context) != newLocaleToDisplay

        if(newLocaleCode != impl.getLocale(context)) {
            impl.setLocale(newLocaleCode, context)
        }

        view.takeIf { needsRestart }?.restartUI()
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
