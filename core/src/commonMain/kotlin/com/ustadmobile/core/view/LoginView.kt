package com.ustadmobile.core.view

import kotlin.js.JsName

interface LoginView : UstadViewWithSnackBar {

    @JsName("setInProgress")
    fun setInProgress(inProgress: Boolean)

    @JsName("setErrorMessage")
    fun setErrorMessage(errorMessage: String)

    @JsName("setServerUrl")
    fun setServerUrl(serverUrl: String)

    @JsName("setUserAccount")
    fun setUsername(username: String)

    @JsName("setPassword")
    fun setPassword(password: String)

    @JsName("showRegisterCodeDialog")
    fun showRegisterCodeDialog(title: String, okButtonText: String, cancelButtonText: String)

    @JsName("setRegistrationLinkVisible")
    fun setRegistrationLinkVisible(visible: Boolean)

    fun setMessage(message:String)

    fun setFinishAfficinityOnView()

    fun forceSync()

    fun updateVersionOnLogin(version: String)

    fun showToolbar(show: Boolean)

    fun updateLastActive()

    fun updateUsername(username: String)

    companion object {

        const val VIEW_NAME = "Login"
        const val ARG_LOGIN_USERNAME = "LoginUsername"
        val ARG_STARTSYNCING = "argStatSync"
    }

}
