package com.ustadmobile.core.view

interface SettingsView : UstadView {

    var workspaceSettingsVisible: Boolean

    var holidayCalendarVisible: Boolean

    var reasonLeavingVisible: Boolean

    var langListVisible: Boolean

    fun setLanguageOptions(languages: List<String>, currentSelection: String)
    fun restartUI()

    companion object {
        val VIEW_NAME = "SettingsFromToolbar"
    }
}
