package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.UmAccount

interface Login2View : UstadView {

    var isEmptyPassword: Boolean

    var isEmptyUsername: Boolean

    var inProgress: Boolean

    var createAccountVisible:Boolean

    var connectAsGuestVisible: Boolean

    fun clearFields()

    fun navigateToNextDestination(account: UmAccount?,fromDestination: String, nextDestination: String)

    var errorMessage: String

    var versionInfo: String?

    companion object {

        const val VIEW_NAME = "Login2View"

    }

}
