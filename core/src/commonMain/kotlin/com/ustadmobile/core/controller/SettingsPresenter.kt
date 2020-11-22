package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import org.kodein.di.DI
import org.kodein.di.instance

class SettingsPresenter(context: Any, arguments: Map<String, String>?, view: SettingsView,
                        di: DI) :
        UstadBaseController<SettingsView>(context, arguments!!, view, di) {

    private val impl: UstadMobileSystemImpl by instance()

    fun goToHolidayCalendarList() {
        val args = HashMap<String, String>()
        impl.go(HolidayCalendarListView.VIEW_NAME, args, context)
    }

    fun goToRolesList() {
        val args = HashMap<String, String>()
        impl.go(RoleListView.VIEW_NAME, args, context)
    }

    fun goToPeopleList() {
        val args = HashMap<String, String>()
        impl.go(PersonListView.VIEW_NAME, args, context)
    }

    fun handleClickNetworkNodeList() {
        impl.go(NetworkNodeListView.VIEW_NAME, mapOf(), context)
    }

}
