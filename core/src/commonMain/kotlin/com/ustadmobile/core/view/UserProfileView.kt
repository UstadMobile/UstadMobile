package com.ustadmobile.core.view

interface UserProfileView : UstadView {

    fun finish()

    fun updateToolbarTitle(personName: String)

    fun setUsername(username: String)

    fun setCurrentLanguage(language: String?)

    fun setLanguageOption(languages: MutableList<String>)

    fun updateImageOnView(imagePath: String, skipCached: Boolean)

    fun addImageFromCamera()

    fun addImageFromGallery()

    fun sendMessage(messageId: Int)

    fun restartUI()

    fun callFinishAffinity()

    fun showLanguageOptions()

    companion object {

        const val VIEW_NAME = "UserProfile"

        const val PERSON_UID = "person_uid"
    }
}
