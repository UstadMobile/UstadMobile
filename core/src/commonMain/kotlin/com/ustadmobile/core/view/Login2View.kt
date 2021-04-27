package com.ustadmobile.core.view

interface Login2View : UstadView {

    var isEmptyPassword: Boolean

    var isEmptyUsername: Boolean

    var inProgress: Boolean

    var createAccountVisible:Boolean

    var connectAsGuestVisible: Boolean

    fun clearFields()

    var errorMessage: String

    var versionInfo: String?

    companion object {

        const val VIEW_NAME = "Login2View"

        const val ARG_NO_GUEST = "NoGuest"

    }

}
