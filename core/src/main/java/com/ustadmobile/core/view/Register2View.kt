package com.ustadmobile.core.view

interface Register2View : UstadView {

    fun setErrorMessageView(errorMessageView: String)

    fun setServerUrl(url: String)

    fun setInProgress(inProgress: Boolean)

    companion object {

        val VIEW_NAME = "RegisterAccount"

        val FIELD_FIRST_NAME = 1

        val FIELD_LAST_NAME = 2

        val FIELD_USERNAME = 3

        val FIELD_EMAIL = 4

        val FIELD_PASSWORD = 5

        val FIELD_CONFIRM_PASSWORD = 6
    }

}
