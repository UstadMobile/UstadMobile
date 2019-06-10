package com.ustadmobile.core.view

import kotlin.js.JsName

interface Register2View : UstadView {

    @JsName("setErrorMessageView")
    fun setErrorMessageView(errorMessageView: String)

    @JsName("setServerUrl")
    fun setServerUrl(url: String)

    @JsName("setInProgress")
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
