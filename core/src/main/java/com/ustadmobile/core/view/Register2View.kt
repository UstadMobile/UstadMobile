package com.ustadmobile.core.view

interface Register2View : UstadView {

    fun setErrorMessageView(errorMessageView: String)

    fun setServerUrl(url: String)

    fun setInProgress(inProgress: Boolean)

    companion object {

        const val VIEW_NAME = "RegisterAccount"

        const val FIELD_FIRST_NAME = 1

        const val FIELD_LAST_NAME = 2

        const val FIELD_USERNAME = 3

        const val FIELD_EMAIL = 4

        const val FIELD_PASSWORD = 5

        const val FIELD_CONFIRM_PASSWORD = 6
    }

}
