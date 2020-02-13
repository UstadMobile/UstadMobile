package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*

class SettingsPresenter(context: Any, arguments: Map<String, String>?, view: SettingsView,
                        val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<SettingsView>(context, arguments!!, view) {

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
    }

    fun goToSELQuestionSets() {
        val args = HashMap<String, String>()
        impl.go(SELQuestionSetsView.VIEW_NAME, args, context)
    }

    fun goToHolidayCalendarList() {
        val args = HashMap<String, String>()
        impl.go(HolidayCalendarListView.VIEW_NAME, args, context)
    }

    fun goToRolesList() {
        val args = HashMap<String, String>()
        impl.go(RoleListView.VIEW_NAME, args, context)
    }

    fun goToGroupsList() {
        val args = HashMap<String, String>()
        impl.go(GroupListView.VIEW_NAME, args, context)
    }

    fun goToRolesAssignmentList() {
        val args = HashMap<String, String>()
        impl.go(RoleAssignmentListView.VIEW_NAME, args, context)
    }

    fun goToLocationsList() {
        val args = HashMap<String, String>()
        impl.go(LocationListView.VIEW_NAME, args, context)
    }

    fun goToPeopleList() {
        val args = HashMap<String, String>()
        impl.go(PeopleListView.VIEW_NAME, args, context)
    }

    fun goToAuditLogSelection() {
        val args = HashMap<String, String>()
        impl.go(AuditLogSelectionView.VIEW_NAME, args, context)
    }

    fun goToCustomFieldsList() {
        val args = HashMap<String, String>()
        impl.go(CustomFieldPersonListView.VIEW_NAME, args, context)

    }

}
