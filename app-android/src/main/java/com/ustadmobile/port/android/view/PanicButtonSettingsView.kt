package com.ustadmobile.port.android.view

import com.ustadmobile.core.view.UstadView
import com.ustadmobile.port.android.presenter.PanicTriggerApp

interface PanicButtonSettingsView: UstadView {

    var selectedTriggerApp: PanicTriggerApp?

    var panicTriggerAppList: List<PanicTriggerApp>

    var unhideCode: String?

    companion object {

        const val VIEW_NAME = "PanicButtonSettings"
    }
}