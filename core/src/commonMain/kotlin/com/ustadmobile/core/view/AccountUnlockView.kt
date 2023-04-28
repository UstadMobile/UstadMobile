package com.ustadmobile.core.view

interface AccountUnlockView : UstadView {

    var accountName: String?

    var error: String?

    companion object {

        const val VIEW_NAME = "AccountUnlock"

    }

}