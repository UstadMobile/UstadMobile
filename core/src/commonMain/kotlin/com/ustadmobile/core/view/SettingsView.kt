package com.ustadmobile.core.view

interface SettingsView : UstadView {

    var workspaceSettingsVisible: Boolean

    companion object {
        val VIEW_NAME = "SettingsFromToolbar"
    }
}
