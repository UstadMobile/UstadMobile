package com.ustadmobile.core.view

interface UserProfileView : UstadView {

    fun setUsername(username: String)

    fun setCurrentLanguage(language: String?)

    fun setLanguageOption(languages: MutableList<String>)

    fun restartUI()

    fun showLanguageOptions()

    companion object {

        const val VIEW_NAME = "UserProfile"

        const val PERSON_UID = "person_uid"
    }
}
