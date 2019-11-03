package com.ustadmobile.core.view

/**
 * Core View. Screen is for UserProfile's View
 */
interface UserProfileView : UstadView {

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun updateToolbarTitle(personName: String)

    fun setLanguageSet(languageSet: String)

    fun setLanguageOption(languages: MutableList<String>)

    fun updateImageOnView(imagePath: String)

    fun addImageFromCamera()

    fun addImageFromGallery()

    fun sendMessage(messageId: Int)

    fun updateLastSyncedText(lastSynced: String)

    fun callFinishAffinity()

    fun restartUI()

    fun showLanguageOptions()

    companion object {

        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "UserProfile"
    }


}


