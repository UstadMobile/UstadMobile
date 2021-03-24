package com.ustadmobile.core.view

interface SettingsView : UstadView {

    var workspaceSettingsVisible: Boolean
    var holidayCalendarVisible: Boolean
    var rolesVisible: Boolean
    var reasonLeavingVisible: Boolean

    companion object {
        val VIEW_NAME = "SettingsFromToolbar"
    }
}
