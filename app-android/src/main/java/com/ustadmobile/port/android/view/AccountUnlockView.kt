package com.ustadmobile.port.android.view

import com.ustadmobile.core.view.UstadView

interface AccountUnlockView : UstadView {

    var accountName: String?

    companion object {

        const val VIEW_NAME = "AccountUnlock"

    }

}