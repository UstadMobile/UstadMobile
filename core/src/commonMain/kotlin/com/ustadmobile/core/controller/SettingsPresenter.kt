package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*

class SettingsPresenter(context: Any, arguments: Map<String, String>?, view: SettingsView,
                        val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<SettingsView>(context, arguments!!, view) {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    fun goToSELQuestionSets() {
        val args = HashMap<String, String>()
    }

    fun goToHolidayCalendarList() {
        val args = HashMap<String, String>()
        impl.go(HolidayCalendarListView.VIEW_NAME, args, context)
    }

    fun goToRolesList() {
        val args = HashMap<String, String>()
    }

    fun goToGroupsList() {
        val args = HashMap<String, String>()
    }

    fun goToRolesAssignmentList() {
        val args = HashMap<String, String>()
    }

    fun goToLocationsList() {
        val args = HashMap<String, String>()
    }

    fun goToPeopleList() {
        val args = HashMap<String, String>()
        impl.go(PersonListView.VIEW_NAME, args, context)
    }

    fun goToAuditLogSelection() {
        val args = HashMap<String, String>()
    }

    fun goToCustomFieldsList() {
        val args = HashMap<String, String>()
    }


}
