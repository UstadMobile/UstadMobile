package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import org.kodein.di.DI
import org.kodein.di.instance

class SettingsPresenter(context: Any, arguments: Map<String, String>, view: SettingsView,
                        di: DI) :
        UstadBaseController<SettingsView>(context, arguments, view, di) {

    private val impl: UstadMobileSystemImpl by instance()

    fun goToHolidayCalendarList() {
        impl.go(HolidayCalendarListView.VIEW_NAME, mapOf(), context)
    }

    fun goToRolesList() {
        impl.go(RoleListView.VIEW_NAME, mapOf(), context)
    }

    fun goToPeopleList() {
        impl.go(PersonListView.VIEW_NAME, mapOf(), context)
    }

    fun handleClickWorkspace() {
        impl.go(WorkspaceDetailView.VIEW_NAME, mapOf(), context)
    }

}
