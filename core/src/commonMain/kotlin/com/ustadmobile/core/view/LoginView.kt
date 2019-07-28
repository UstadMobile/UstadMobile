package com.ustadmobile.core.view

import kotlin.js.JsName

interface LoginView : UstadView {

    @JsName("setInProgress")
    fun setInProgress(inProgress: Boolean)

    @JsName("setErrorMessage")
    fun setErrorMessage(errorMessage: String)

    @JsName("setServerUrl")
    fun setServerUrl(serverUrl: String)

    @JsName("setUsername")
    fun setUsername(username: String)

    @JsName("setPassword")
    fun setPassword(password: String)

    @JsName("setRegistrationLinkVisible")
    fun setRegistrationLinkVisible(visible: Boolean)

    companion object {

        const val VIEW_NAME = "Login"
    }

}
