package com.ustadmobile.core.view

interface Login2View : UstadView {

    fun setInProgress(inProgress: Boolean)

    fun setErrorMessage(errorMessage: String)

    fun setServerUrl(serverUrl: String)

    fun setUsername(username: String)

    fun setPassword(password: String)

    companion object {

        const val VIEW_NAME = "Login2"
    }

}
