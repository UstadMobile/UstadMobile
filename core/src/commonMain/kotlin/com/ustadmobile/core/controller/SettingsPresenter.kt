package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.GroupListView
import com.ustadmobile.core.view.LocationListView
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.SettingsView


class SettingsPresenter(context: Any, arguments: Map<String, String>?, view: SettingsView,
                        val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<SettingsView>(context, arguments!!, view) {

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
    }

    fun goToGroupsList() {
        val args = HashMap<String, String>()
        impl.go(GroupListView.VIEW_NAME, args, context)
    }

    fun goToLocationsList() {
        val args = HashMap<String, String>()
        impl.go(LocationListView.VIEW_NAME, args, context)
    }

    fun goToPeopleList(){
        val args = HashMap<String, String>()
        impl.go(PersonListView.VIEW_NAME, args, context)
    }


}
