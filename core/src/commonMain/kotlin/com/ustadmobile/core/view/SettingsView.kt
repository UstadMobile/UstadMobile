package com.ustadmobile.core.view

interface SettingsView : UstadView {

    var workspaceSettingsVisible: Boolean

    var holidayCalendarVisible: Boolean

    var reasonLeavingVisible: Boolean

    var langListVisible: Boolean

    var displayLanguage: String?

    companion object {
        val VIEW_NAME = "SettingsFromToolbar"
    }
}
