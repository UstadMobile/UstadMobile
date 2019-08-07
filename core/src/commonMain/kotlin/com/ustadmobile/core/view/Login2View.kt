package com.ustadmobile.core.view

interface Login2View : UstadView {

    fun setInProgress(inProgress: Boolean)

    fun setErrorMessage(errorMessage: String)

    fun setServerUrl(serverUrl: String)

    fun setUsername(username: String)

    fun setPassword(password: String)

    fun setFinishAfficinityOnView()

    fun forceSync()

    fun updateVersionOnLogin(version: String)

    companion object {

        val VIEW_NAME = "Login2"

        val ARG_STARTSYNCING = "argStatSync"
    }

}
