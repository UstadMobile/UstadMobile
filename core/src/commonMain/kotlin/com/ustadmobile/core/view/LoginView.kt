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

    fun forceSync()

    fun updateLastActive()

    fun updateUsername(username: String)

    fun setFinishAfficinityOnView()

    companion object {

        const val VIEW_NAME = "Login"
        const val ARG_LOGIN_USERNAME = "LoginUsername"
    }

}
